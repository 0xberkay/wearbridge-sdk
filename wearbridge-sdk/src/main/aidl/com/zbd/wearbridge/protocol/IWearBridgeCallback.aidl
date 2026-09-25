package com.zbd.wearbridge.protocol;

import com.zbd.wearbridge.protocol.TaskResult;

oneway interface IWearBridgeCallback {
    void onPermissionResult(int requestCode, int result);
    void onTaskResult(in TaskResult result);
    void onServerDisconnected();
}
