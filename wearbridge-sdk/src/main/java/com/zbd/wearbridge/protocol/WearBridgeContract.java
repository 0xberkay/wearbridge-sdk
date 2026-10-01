package com.zbd.wearbridge.protocol;

public final class WearBridgeContract {
    public static final int PROTOCOL_VERSION = 2;
    public static final int SDK_VERSION = 2;

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
    public static final String MUTATING_TASK_ACTION = "com.zbd.wearbridge.action.SHOW_MUTATING_TASK";
    public static final String PERMISSION_ACTIVITY_CLASS = "com.zbd.wearbridge.presentation.MainActivity";

    public static final String TASK_PING = "core.ping";
    public static final String TASK_CAPABILITIES = "core.capabilities";
    public static final String TASK_DIAGNOSTICS = "core.diagnostics";

    /** Runs a fixed dumpsys section under the ADB shell identity. Unavailable without the shell server. */
    public static final String TASK_DUMPSYS = "adb.dumpsys";

    // ---- Display (v2 catalog; all require the shell server) ----

    /** Runs `wm density` and reports physical vs override density. */
    public static final String TASK_DENSITY_GET = "display.density.get";
    /** Runs `wm density <dpi>` with dpi coerced to 120-640. Mutating: confirmed on-watch per execution. */
    public static final String TASK_DENSITY_SET = "display.density.set";
    /** Runs `wm density reset`. Mutating: confirmed on-watch per execution. */
    public static final String TASK_DENSITY_RESET = "display.density.reset";

    // ---- Animation scale ----

    /** Reads window/transition/animator scales. */
    public static final String TASK_ANIM_GET = "ui.anim.get";
    /** Writes all three animation scales (0-10). Mutating: confirmed on-watch per execution. */
    public static final String TASK_ANIM_SET = "ui.anim.set";

    // ---- Connectivity ----

    /** Runs `svc wifi enable|disable`. Mutating: confirmed on-watch per execution. */
    public static final String TASK_WIFI_SET = "net.wifi.set";
    /** Runs `svc bluetooth enable|disable`. Mutating: confirmed on-watch per execution. */
    public static final String TASK_BT_SET = "net.bt.set";
    /** Toggles airplane mode. Mutating: confirmed on-watch per execution. */
    public static final String TASK_AIRPLANE_SET = "net.airplane.set";
    /** Reads private-DNS mode and hostname. */
    public static final String TASK_PRIVATEDNS_GET = "net.privatedns.get";
    /** Sets private-DNS mode (off/automatic/hostname). Mutating: confirmed on-watch per execution. */
    public static final String TASK_PRIVATEDNS_SET = "net.privatedns.set";
    /** Per-package network block/allow (`cmd connectivity`). Mutating: confirmed on-watch per execution. */
    public static final String TASK_FIREWALL_SET = "net.firewall.set";
    /** Reads whether per-package blocking is armed. */
    public static final String TASK_FIREWALL_STATUS = "net.firewall.status";

    // ---- Apps ----

    /** Capped `dumpsys package` excerpt for one package. */
    public static final String TASK_PERM_LIST = "privacy.perm.list";
    /** `am force-stop` for one package. Mutating: confirmed on-watch per execution. */
    public static final String TASK_FORCE_STOP = "apps.force.stop";

    /** Argument keys for the v2 catalog. Values travel as shell-safe strings, never shell text. */
    public static final String ARG_DPI = "dpi";
    public static final String ARG_SCALE = "scale";
    public static final String ARG_ENABLED = "enabled";
    public static final String ARG_MODE = "mode";
    public static final String ARG_HOSTNAME = "hostname";
    public static final String ARG_PACKAGE = "package";

    private WearBridgeContract() {}
}
