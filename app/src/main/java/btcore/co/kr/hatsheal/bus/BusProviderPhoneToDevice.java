package btcore.co.kr.hatsheal.bus;

/**
 * Created by leehaneul on 2018-04-21.
 */

public class BusProviderPhoneToDevice {

    private static final CustomBus BUS = new CustomBus();

    public static CustomBus getInstance() {
        return BUS;
    }

    private BusProviderPhoneToDevice() {
    }

}
