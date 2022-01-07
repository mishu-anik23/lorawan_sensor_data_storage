/**
 * This program implements the Java version of Milesight AM100 Series LoRaWAN Sensor decoder.
 * The decoding concept is inherited from Public Milesight sensors decoder repo,
 * which implements Chirpstack and TTN compatible Javascript decoders. Please see in following link:
 * https://github.com/Milesight-IoT/SensorDecoders/tree/master/AM100_Series
 *
 * @author Anik Mishu (anpaul098@gmail.com)
 * @version 1.0
 * @since   2022-01-05
 */
package com.loradata.devicedecoders.milesight;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MsAM100Decoder{
    private static int readUInt16LE(int[] bytes){
        int value = (bytes[1] << 8) + bytes[0];
        return value & 0xffff;
    }
    private static int readInt16LE(int[] bytes){
        int value = readUInt16LE(bytes);
        return value > 0x7fff ? value - 0x10000 : value;
    }
    private static int[] hexStringToIntArray(String s) {
        /*
        Converts a given Hex String to corresponding Integer Array. Thanks to hex str to byte stackoverflow:
        https://stackoverflow.com/questions/8890174/in-java-how-do-i-convert-a-hex-string-to-a-byte

        @param      String  s   Any given Hex String.
        @return     int [] data Integer Array of given string.     .
         */
        int len = s.length();
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    private String intArrayToHexString(int[] arr) {
        StringBuilder builder = new StringBuilder();
        for (int b : arr) {
            builder.append(Integer.toHexString(b));
        }
        return builder.toString();
    }
    private Map<String, String> decodeBasicInfo(String rawData, int fPort){
        /*
        Decode basic sensor info from raw Hex string during everytime join in LNS.

        --------------------- Payload Definition ---------------------
            ff0112ff086128a5294269ff090140ff0a0142ff0f00ff18007b

                            [channel_id] [channel_type] [Channel_value]
        protocol_version    -> 0xff         0x01        [1byte]     12
        device_sn           -> 0xff         0x08        [6bytes]    6128a5294269
        hw_version          -> 0xff         0x09        [2bytes]    01 40 => V1.4
        sw_version          -> 0xff         0x0a        [2bytes]    01 42 => V1.42
        device_type         -> 0xff         0x0f        [1byte]     00: Class A, 01: Class B, 02: Class C

        @param      String  rawData     Hex String sent by sensor as Uplink
        @return     Map<String, String> decoded basic info a/c to chart.
         */
        Map<String, String> decodedBasicInfo = new HashMap<>();
        int[] bytesBasicInfo = hexStringToIntArray(rawData);
        for (int i=0; i<bytesBasicInfo.length;){
            int chId = bytesBasicInfo[i++];
            int chType = bytesBasicInfo[i++];
            if (chId == Integer.parseInt("ff", 16) && chType == Integer.parseInt("01", 16)){
                decodedBasicInfo.put("protocol_version", "V" + bytesBasicInfo[i]);
                i += 1;
            }
            else if (chId == Integer.parseInt("ff", 16) && chType == Integer.parseInt("08", 16)){
                decodedBasicInfo.put("device_sn",  intArrayToHexString(Arrays.copyOfRange(bytesBasicInfo, i, i+6)));
                i += 6;
            }
            else if (chId == Integer.parseInt("ff", 16) && chType == Integer.parseInt("09", 16)){
                decodedBasicInfo.put("hw_version",  "V"+bytesBasicInfo[i]+"."+Integer.toHexString(bytesBasicInfo[i+1]));
                i += 2;
            }
            else if (chId == Integer.parseInt("ff", 16) && chType == Integer.parseInt("0a", 16)){
                decodedBasicInfo.put("sw_version",  "V"+bytesBasicInfo[i]+"."+Integer.toHexString(bytesBasicInfo[i+1]));
                i += 2;
            }
            else if (chId == Integer.parseInt("ff", 16) && chType == Integer.parseInt("0f", 16)){
                int val = bytesBasicInfo[i];
                decodedBasicInfo.put("device_type",  val == 0 ? "A": val == 1 ? "B": "C");
                i += 2;
            }
            else {
                break;
            }
        }
        return decodedBasicInfo;
    }
    private Map<String, String> decodeSensorData(String rawData, int fPort){
        /*
        Decode regular uplink sensor measurement from raw Hex String.

        --------------------- Payload Definition ---------------------
        01755C03673401046865056A490006651C0079001400077DE704087D070009733F27

                           [channel_id] [channel_type] [channel_value]
        battery      -> 0x01         0x75          [1byte ] Unit: %
        temperature  -> 0x03         0x67          [2bytes] Unit: °C (℉)
        humidity     -> 0x04         0x68          [1byte ] Unit: %RH
        activity     -> 0x05         0x6A          [2bytes] Unit:
        illumination -> 0x06         0x65          [6bytes] Unit: lux
        ------------------------------------------ AM104

        CO2          -> 0x07         0x7D          [2bytes] Unit: ppm
        tVOC         -> 0x08         0x7D          [2bytes] Unit: ppb
        pressure     -> 0x09         0x73          [2bytes] Unit: hPa
        ------------------------------------------ AM107

        @param      String  rawData     Hex String sent by sensor as Uplink
        @return     Map<String, String> decoded regular sensor measurement data a/c to chart.
         */
        Map<String, String> decodedData = new HashMap<>();
        int[] bytesRawData = hexStringToIntArray(rawData);
        for (int i=0; i<bytesRawData.length;) {
            int chId = bytesRawData[i++];
            int chType = bytesRawData[i++];
            if (chId == Integer.parseInt("01", 16) && chType == Integer.parseInt("75", 16)){
                decodedData.put("battery", Float.toString((float) bytesRawData[i]/100 + (float) 2.5));
                i += 1;
            }
            else if (chId == Integer.parseInt("03", 16) && chType == Integer.parseInt("67", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                decodedData.put("temperature", Float.toString((float) readInt16LE(bytesMeasValue)/10));
                i += 2;
            }
            else if (chId == Integer.parseInt("04", 16) && chType == Integer.parseInt("68", 16)){
                decodedData.put("humidity", Float.toString((float) bytesRawData[i]/2));
                i += 1;
            }
            else if (chId == Integer.parseInt("05", 16) && chType == Integer.parseInt("6A", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                decodedData.put("activity", Integer.toString(readUInt16LE(bytesMeasValue)));
                i += 2;
            }
            else if (chId == Integer.parseInt("06", 16) && chType == Integer.parseInt("65", 16)){
                int[] bytesMeasValueIllumination = Arrays.copyOfRange(bytesRawData, i, i+2);
                int[] bytesMeasValueIRVisible = Arrays.copyOfRange(bytesRawData, i+2, i+4);
                int[] bytesMeasValueIR = Arrays.copyOfRange(bytesRawData, i+4, i+6);

                decodedData.put("illumination", Integer.toString(readUInt16LE(bytesMeasValueIllumination)));
                decodedData.put("infrared_visible", Integer.toString(readUInt16LE(bytesMeasValueIRVisible)));
                decodedData.put("infrared", Integer.toString(readUInt16LE(bytesMeasValueIR)));
                i += 6;
            }
            else if (chId == Integer.parseInt("07", 16) && chType == Integer.parseInt("7D", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                decodedData.put("co2", Integer.toString(readUInt16LE(bytesMeasValue)));
                i += 2;
            }
            else if (chId == Integer.parseInt("08", 16) && chType == Integer.parseInt("7D", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                decodedData.put("tvoc", Integer.toString(readUInt16LE(bytesMeasValue)));
                i += 2;
            }
            else if (chId == Integer.parseInt("09", 16) && chType == Integer.parseInt("73", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                decodedData.put("pressure", Float.toString((float) readUInt16LE(bytesMeasValue)/10));
                i += 2;
            }
            else {
                break;
            }
        }
        return decodedData;
    }
    public Map<String, String> decodeUplink(String rawData, int fPort){
        /*
        Decode Uplink message both for basic info and regular measurement data.
        Currently, function call distinguishion is based on checking the first byte of uplink message.

        @param      String  rawData     Hex String sent by sensor as Uplink
        @return     Map<String, String> decoded uplink message based on function call.
         */
        Map<String, String> decodedUplink;
        if (rawData.toUpperCase().startsWith("FF")){
            decodedUplink = this.decodeBasicInfo(rawData, 85);
        }
        else {
            decodedUplink = this.decodeSensorData(rawData, 85);
        }
        return decodedUplink;
    }
}
