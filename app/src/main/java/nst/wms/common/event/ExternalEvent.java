package nst.wms.common.event;

public interface ExternalEvent {

    /** Target topic for routing (e.g. "users"). */
    String getEventTarget();

    /** Message key for partitioning/ordering (e.g. userId.toString()). */
    String getEventKey();
}
