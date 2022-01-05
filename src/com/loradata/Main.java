package com.loradata;

import com.loradata.devicedecoders.milesight.MsAM100Decoder;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
	// write your code here
        String rawData = "01755C03673401046865056A490006651C0079001400077DE704087D070009733F27";
        String rawBasicInfo = "ff01abff086128a5294269ff090140ff0a0142ff0f00ff18007b";
        MsAM100Decoder msam100decoder = new MsAM100Decoder();
        Map<String, String> decodedUplinkData = msam100decoder.decodeUplink(rawData, 85);
        Map<String, String> decodedBasicInfo = msam100decoder.decodeUplink(rawBasicInfo, 85);
        System.out.println(decodedUplinkData);
        System.out.println(decodedBasicInfo);
    }
}
