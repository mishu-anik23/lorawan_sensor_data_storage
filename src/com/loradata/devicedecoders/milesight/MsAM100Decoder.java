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
