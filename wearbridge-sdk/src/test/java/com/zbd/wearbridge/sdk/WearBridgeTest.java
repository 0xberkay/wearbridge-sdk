package com.zbd.wearbridge.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;

import com.zbd.wearbridge.protocol.TaskRequest;
import com.zbd.wearbridge.protocol.TaskResult;
import com.zbd.wearbridge.protocol.TaskStatus;
import com.zbd.wearbridge.protocol.WearBridgeContract;

import org.junit.Test;

public class WearBridgeTest {

    @Test
    public void testTaskStatusConstants() {
        assertEquals(0, TaskStatus.OK);
        assertEquals(1, TaskStatus.SERVER_UNAVAILABLE);
        assertEquals(2, TaskStatus.ADB_NOT_PAIRED);
        assertEquals(3, TaskStatus.PERMISSION_DENIED);
        assertEquals(4, TaskStatus.PARTNER_NOT_ALLOWED);
        assertEquals(5, TaskStatus.TASK_NOT_ALLOWED);
        assertEquals(6, TaskStatus.INVALID_ARGUMENT);
        assertEquals(7, TaskStatus.CAPABILITY_UNAVAILABLE);
        assertEquals(8, TaskStatus.USER_CANCELLED);
        assertEquals(9, TaskStatus.EXECUTION_FAILED);
    }

    @Test
    public void testWearBridgeContractConstants() {
        assertEquals("com.zbd.wearbridge", WearBridgeContract.MANAGER_PACKAGE);
        assertEquals("com.zbd.wearbridge.action.BIND_API", WearBridgeContract.BROKER_ACTION);
        assertEquals("com.zbd.wearbridge.service.WearBridgeBrokerService", WearBridgeContract.BROKER_CLASS);
        assertEquals("com.zbd.wearbridge.action.SHOW_PERMISSION", WearBridgeContract.PERMISSION_ACTION);
        assertEquals("com.zbd.wearbridge.presentation.MainActivity", WearBridgeContract.PERMISSION_ACTIVITY_CLASS);

        assertEquals("core.ping", WearBridgeContract.TASK_PING);
        assertEquals("core.capabilities", WearBridgeContract.TASK_CAPABILITIES);
        assertEquals("core.diagnostics", WearBridgeContract.TASK_DIAGNOSTICS);
        assertEquals("adb.dumpsys", WearBridgeContract.TASK_DUMPSYS);

        assertEquals(0, WearBridgeContract.PERMISSION_GRANTED);
        assertEquals(-1, WearBridgeContract.PERMISSION_DENIED);
    }
}
