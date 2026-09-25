package com.zbd.wearbridge.protocol;

import com.zbd.wearbridge.protocol.TaskRequest;
import com.zbd.wearbridge.protocol.IWearBridgeCallback;

interface IWearBridgeService {
    int getProtocolVersion();
    int checkPermission(String packageName);
    void execute(String packageName, in TaskRequest request, in IWearBridgeCallback callback);
}
