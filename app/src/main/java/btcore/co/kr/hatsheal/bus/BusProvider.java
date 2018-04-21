package btcore.co.kr.hatsheal.bus;

/**
 * Created by leehaneul on 2018-01-31.
 */

public final class BusProvider extends CustomBus {
    private static final CustomBus BUS = new CustomBus();

    public static CustomBus getInstance() {
        return BUS;
    }

    private BusProvider() {
    }
}
