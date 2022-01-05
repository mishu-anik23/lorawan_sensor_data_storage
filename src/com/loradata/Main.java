package com.loradata;

import com.loradata.devicedecoders.milesight.MsAM100Decoder;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
	// write your code here
        String rawData = "01755C03673401046865056A490006651C0079001400077DE704087D070009733F27";
        MsAM100Decoder msam100decoder = new MsAM100Decoder();
        Map<String, String> decodedData = msam100decoder.decodeUplink(rawData, 85);
        System.out.println(decodedData);
    }
}
