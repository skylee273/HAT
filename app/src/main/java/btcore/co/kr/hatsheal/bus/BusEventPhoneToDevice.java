package btcore.co.kr.hatsheal.bus;

/**
 * Created by leehaneul on 2018-04-21.
 */

public class BusEventPhoneToDevice {

    private String eventData;
    private int eventType;

    public BusEventPhoneToDevice(String eventData, int type) {
        this.eventData = eventData;
        this.eventType = type;
    }

    public String getEventData() {
        return eventData;
    }
    public int getEventType() {
        return eventType;
    }
}
