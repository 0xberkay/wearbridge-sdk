package com.zbd.wearbridge.protocol;

public final class TaskStatus {
    public static final int OK = 0;
    public static final int SERVER_UNAVAILABLE = 1;
    public static final int ADB_NOT_PAIRED = 2;
    public static final int PERMISSION_DENIED = 3;
    public static final int PARTNER_NOT_ALLOWED = 4;
    public static final int TASK_NOT_ALLOWED = 5;
    public static final int INVALID_ARGUMENT = 6;
    public static final int CAPABILITY_UNAVAILABLE = 7;
    public static final int USER_CANCELLED = 8;
    public static final int EXECUTION_FAILED = 9;
    /** Non-terminal result: the SDK must open the manager's confirmation screen. */
    public static final int APPROVAL_REQUIRED = 10;

    private TaskStatus() {}
}
