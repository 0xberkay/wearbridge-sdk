package com.zbd.wearbridge.protocol;

import com.zbd.wearbridge.protocol.IWearBridgeCallback;
import com.zbd.wearbridge.protocol.IWearBridgeService;

interface IWearBridgeBroker {
    IWearBridgeService connect(String packageName, in IWearBridgeCallback callback);
    int checkPermission(String packageName);
    void requestPermission(String packageName, int requestCode, in IWearBridgeCallback callback);
}
