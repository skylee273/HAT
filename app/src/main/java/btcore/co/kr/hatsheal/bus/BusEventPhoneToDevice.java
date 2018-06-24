package btcore.co.kr.hatsheal.bus;

/**
 * Created by leehaneul on 2018-04-21.
 */

public class BusEventPhoneToDevice {

    private byte [] eventData;
    private int eventType;

    public BusEventPhoneToDevice(byte [] eventData, int type) {
        this.eventData = eventData;
        this.eventType = type;
    }

    public byte[] getEventData() {
        return eventData;
    }
    public int getEventType() {
        return eventType;
    }
}
