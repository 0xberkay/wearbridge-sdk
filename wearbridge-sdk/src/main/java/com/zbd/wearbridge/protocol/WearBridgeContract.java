package com.zbd.wearbridge.protocol;

public final class WearBridgeContract {
    public static final int PROTOCOL_VERSION = 2;
    public static final int SDK_VERSION = 1;

    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_GRANTED = 0;

    public static final String MANAGER_PACKAGE = "com.zbd.wearbridge";
    public static final String BROKER_ACTION = "com.zbd.wearbridge.action.BIND_API";
    public static final String BROKER_CLASS = "com.zbd.wearbridge.service.WearBridgeBrokerService";

    /**
     * Consent screen in the manager. The partner starts it itself: a bound service may not launch
     * an activity from the background, so the manager cannot raise its own dialog on request.
     */
    public static final String PERMISSION_ACTION = "com.zbd.wearbridge.action.SHOW_PERMISSION";
    public static final String PERMISSION_ACTIVITY_CLASS = "com.zbd.wearbridge.presentation.MainActivity";

    public static final String TASK_PING = "core.ping";
    public static final String TASK_CAPABILITIES = "core.capabilities";
    public static final String TASK_DIAGNOSTICS = "core.diagnostics";

    /** Runs a fixed dumpsys section under the ADB shell identity. Unavailable without the shell server. */
    public static final String TASK_DUMPSYS = "adb.dumpsys";

    private WearBridgeContract() {}
}
