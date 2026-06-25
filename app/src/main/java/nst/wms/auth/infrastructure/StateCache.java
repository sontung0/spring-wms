package nst.wms.auth.infrastructure;

public interface StateCache {

    void put(String state, String provider);

    String getAndEvict(String state);
}
