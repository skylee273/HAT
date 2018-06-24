package btcore.co.kr.hatsheal.util;

/**
 * Created by leehaneul on 2018-04-20.
 */

public class Protocol {
    private static final byte startPacket = (byte)0xAA;
    private static final byte powerPacket = (byte)0x00;
    private static final byte uvPacket = 0x01;
    private static final byte rgbPacket = 0x03;
    private static final byte streamingPacket = 0x04;
    private static final byte moodLengthPacket = 0x05;
    private static final byte endPacket = (byte)0xff;
    private static final byte onPacket = (byte)0x01;
    private static final byte offPacket = (byte)0x00;

    public byte[] uvControl(){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = uvPacket;        // Type ID
        bytes[2] = onPacket;
        bytes[3] = endPacket;
        return bytes;
    }
    public byte [] uvOffControl(){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = uvPacket;        // Type ID
        bytes[2] = offPacket;
        bytes[3] = endPacket;
        return bytes;
    }
    public byte[] RGBOnControl(){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = rgbPacket;        // Type ID
        bytes[2] = onPacket;
        bytes[3] = endPacket;
        return bytes;
    }
    public byte[] RGBOffControl(){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = rgbPacket;        // Type ID
        bytes[2] = offPacket;
        bytes[3] = endPacket;
        return bytes;
    }
    public byte[] RGBDataStreaming(int red, int green, int blue){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = streamingPacket;        // Type ID
        bytes[2] = (byte)red;
        bytes[3] = (byte)green;
        bytes[4] = (byte)blue;
        bytes[5] = (byte)red;
        bytes[6] = (byte)green;
        bytes[7] = (byte)blue;
        bytes[8] = (byte)red;
        bytes[9] = (byte)green;
        bytes[10] = (byte)blue;
        bytes[11] = (byte)red;
        bytes[12] = (byte)green;
        bytes[13] = (byte)blue;
        bytes[14] = endPacket;
        return bytes;
    }

    public byte[] RGBDataMood(){
        byte[] bytes = new byte[20];
        bytes[0] = startPacket;     // Start ID
        bytes[1] = moodLengthPacket;        // Type ID
        bytes[2] = 0x3a;
        bytes[3] = 0x3a;
        bytes[14] = endPacket;
        return bytes;
    }
}
