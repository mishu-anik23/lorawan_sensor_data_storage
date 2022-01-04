package com.loradata;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
public class MsAM100Decoder{

}
*/
public class Main {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static int[] hexStringToIntArray(String s) {
        int len = s.length();
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static int readUInt16LE(int[] bytes){
        int value = (bytes[1] << 8) + bytes[0];
        return value & 0xffff;
    }
    private static int readInt16LE(int[] bytes){
        int value = readUInt16LE(bytes);
        return value > 0x7fff ? value - 0x10000 : value;
    }

    public static void main(String[] args) {
	// write your code here
        Map<String, String> decodedData = new HashMap<String, String>();
        //decodedData.put("battery", "3.14 V");
        //System.out.println(decodedData.get("battery"));

        String rawData = "01758b03671c010468550665a602370a5401056a1c00077d8001087dbd0009735927";
        char[] charRawData = rawData.toCharArray();
        System.out.println(charRawData);

        //byte[] byteData = hexStringToByteArray(rawData);
        byte[] byteData = new BigInteger(rawData, 16).toByteArray();
        System.out.println(Arrays.toString(byteData));
        int[] bytesRawData = hexStringToIntArray(rawData);
        System.out.println(Arrays.toString(bytesRawData));
        //byte[] byteDataZeroPadded = hexStringToByteArray(rawData);
        //System.out.println(Arrays.toString(byteDataZeroPadded));
        /*
        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println(sb);

         */
        for (int i=0; i<bytesRawData.length;) {
            //String hexStrChannelId = Integer.toHexString(byteData[i++]);
            //String hexStrChannelType = Integer.toHexString(byteData[i++]);
            int chId = bytesRawData[i++];
            int chType = bytesRawData[i++];
            //System.out.println(hexStrChannelId);
            //System.out.println(hexStrChannelType);

            System.out.println(chId);
            System.out.println(chType);
            System.out.println(Integer.parseInt("75", 16));
            if (chId == Integer.parseInt("01", 16) && chType == Integer.parseInt("75", 16)){
                decodedData.put("battery", Integer.toString(bytesRawData[i]));
                i += 1;
            }
            else if (chId == Integer.parseInt("03", 16) && chType == Integer.parseInt("67", 16)){
                int[] bytesMeasValue = Arrays.copyOfRange(bytesRawData, i, i+2);
                System.out.println(Arrays.toString(bytesMeasValue));

                decodedData.put("temperature", Integer.toString(readInt16LE(bytesMeasValue)));
                i += 2;
            }
            else {
                break;
            }
/*
            if (hexStrChannelId.equals("1")){
                //System.out.println(byteData[i]);
                decodedData.put("battery", String.valueOf(byteData[i]));
                i += 1;
            }
            else {
                break;
            }

 */
        }
        //System.out.println(decodedData.get("battery"));
        System.out.println(decodedData);
    }
}
