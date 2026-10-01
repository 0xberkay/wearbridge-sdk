package com.zbd.wearbridge.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import com.zbd.wearbridge.protocol.IWearBridgeBroker;
import com.zbd.wearbridge.protocol.IWearBridgeCallback;
import com.zbd.wearbridge.protocol.IWearBridgeService;
import com.zbd.wearbridge.protocol.TaskRequest;
import com.zbd.wearbridge.protocol.TaskResult;
import com.zbd.wearbridge.protocol.TaskStatus;
import com.zbd.wearbridge.protocol.WearBridgeContract;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WearBridge {
    private static final List<Runnable> readyListeners = new CopyOnWriteArrayList<>();
    private static final List<Runnable> deadListeners = new CopyOnWriteArrayList<>();
    private static final List<PermissionResultListener> permissionListeners = new CopyOnWriteArrayList<>();
    private static final Map<String, TaskCallback> taskCallbacks = new ConcurrentHashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static volatile Context appContext;
    private static volatile IWearBridgeBroker broker;
    private static volatile IWearBridgeService service;
    private static volatile boolean binding;

    private static final IWearBridgeCallback callback = new IWearBridgeCallback.Stub() {
        @Override public void onPermissionResult(int requestCode, int result) {
            mainHandler.post(() -> {
                for (PermissionResultListener listener : permissionListeners) {
                    listener.onPermissionResult(requestCode, result);
                }
            });
        }

        @Override public void onTaskResult(TaskResult result) {
            if (result.getStatus() == TaskStatus.APPROVAL_REQUIRED) {
                if (!taskCallbacks.containsKey(result.getRequestId())) return;
                mainHandler.post(() -> {
                    try {
                        Intent intent = new Intent(WearBridgeContract.MUTATING_TASK_ACTION)
                                .setComponent(new ComponentName(WearBridgeContract.MANAGER_PACKAGE,
                                        WearBridgeContract.PERMISSION_ACTIVITY_CLASS))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        appContext.startActivity(intent);
                    } catch (RuntimeException exception) {
                        // Confirmation remains available when the user opens the manager.
                    }
                });
                return;
            }
            TaskCallback taskCallback = taskCallbacks.remove(result.getRequestId());
            if (taskCallback != null) mainHandler.post(() -> taskCallback.onResult(result));
        }

        @Override public void onServerDisconnected() {
            clearConnection();
        }
    };

    private static final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            binding = false;
            broker = IWearBridgeBroker.Stub.asInterface(binder);
            try {
                service = broker.connect(appContext.getPackageName(), callback);
                if (service == null) {
                    clearConnection();
                    return;
                }
                binder.linkToDeath(WearBridge::clearConnection, 0);
                mainHandler.post(() -> {
                    for (Runnable listener : readyListeners) listener.run();
                });
            } catch (RemoteException | RuntimeException exception) {
                clearConnection();
            }
        }

        @Override public void onServiceDisconnected(ComponentName name) { clearConnection(); }
        @Override public void onBindingDied(ComponentName name) { clearConnection(); }
        @Override public void onNullBinding(ComponentName name) { clearConnection(); }
    };

    private WearBridge() {}

    public static boolean isAvailable(Context context) {
        try {
            context.getPackageManager().getPackageInfo(WearBridgeContract.MANAGER_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static synchronized void bind(Context context) {
        if (service != null || binding) return;
        appContext = context.getApplicationContext();
        Intent intent = new Intent(WearBridgeContract.BROKER_ACTION)
                .setComponent(new ComponentName(WearBridgeContract.MANAGER_PACKAGE, WearBridgeContract.BROKER_CLASS));
        binding = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static synchronized void unbind() {
        if (appContext != null && (service != null || binding)) {
            try { appContext.unbindService(connection); } catch (IllegalArgumentException ignored) {}
        }
        clearConnection();
    }

    public static int checkPermission() {
        IWearBridgeBroker current = broker;
        if (current == null || appContext == null) return WearBridgeContract.PERMISSION_DENIED;
        try {
            return current.checkPermission(appContext.getPackageName());
        } catch (RemoteException exception) {
            clearConnection();
            return WearBridgeContract.PERMISSION_DENIED;
        }
    }

    public static void requestPermission(int requestCode) {
        IWearBridgeBroker current = broker;
        if (current == null || appContext == null) {
            mainHandler.post(() -> {
                for (PermissionResultListener listener : permissionListeners) {
                    listener.onPermissionResult(requestCode, WearBridgeContract.PERMISSION_DENIED);
                }
            });
            return;
        }
        try {
            current.requestPermission(appContext.getPackageName(), requestCode, callback);
            // The manager queued the request but cannot raise its own consent screen: a bound
            // service has no background activity-start privilege. The partner is in the foreground
            // when it asks, so it starts the screen instead.
            Intent intent = new Intent(WearBridgeContract.PERMISSION_ACTION)
                    .setComponent(new ComponentName(
                            WearBridgeContract.MANAGER_PACKAGE, WearBridgeContract.PERMISSION_ACTIVITY_CLASS))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            appContext.startActivity(intent);
        } catch (RemoteException exception) {
            clearConnection();
        } catch (RuntimeException exception) {
            // The manager is present but refused to show the screen; the request stays queued and
            // the user can answer it from the manager directly.
        }
    }

    public static void execute(TaskRequest request, TaskCallback taskCallback) {
        IWearBridgeService current = service;
        if (current == null || appContext == null) {
            mainHandler.post(() -> taskCallback.onResult(TaskResult.error(request, TaskStatus.SERVER_UNAVAILABLE, "WearBridge server is unavailable")));
            return;
        }
        taskCallbacks.put(request.getRequestId(), taskCallback);
        try {
            current.execute(appContext.getPackageName(), request, callback);
        } catch (RemoteException exception) {
            taskCallbacks.remove(request.getRequestId());
            mainHandler.post(() -> taskCallback.onResult(TaskResult.error(request, TaskStatus.SERVER_UNAVAILABLE, "WearBridge connection was lost")));
            clearConnection();
        }
    }

    public static void addBinderReceivedListener(Runnable listener) {
        readyListeners.add(listener);
        if (service != null) mainHandler.post(listener);
    }
    public static void removeBinderReceivedListener(Runnable listener) { readyListeners.remove(listener); }
    public static void addBinderDeadListener(Runnable listener) { deadListeners.add(listener); }
    public static void removeBinderDeadListener(Runnable listener) { deadListeners.remove(listener); }
    public static void addPermissionResultListener(PermissionResultListener listener) { permissionListeners.add(listener); }
    public static void removePermissionResultListener(PermissionResultListener listener) { permissionListeners.remove(listener); }

    private static synchronized void clearConnection() {
        boolean wasConnected = service != null || broker != null;
        service = null;
        broker = null;
        binding = false;
        if (wasConnected) mainHandler.post(() -> {
            for (Runnable listener : deadListeners) listener.run();
        });
    }
}
