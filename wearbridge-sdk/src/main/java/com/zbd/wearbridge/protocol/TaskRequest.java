package com.zbd.wearbridge.protocol;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.UUID;

public final class TaskRequest implements Parcelable {
    private final String requestId;
    private final String taskId;
    private final Bundle arguments;
    private final int sdkVersion;

    public TaskRequest(String requestId, String taskId, Bundle arguments, int sdkVersion) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.arguments = arguments == null ? Bundle.EMPTY : new Bundle(arguments);
        this.sdkVersion = sdkVersion;
    }

    public static TaskRequest create(String taskId, Bundle arguments) {
        return new TaskRequest(UUID.randomUUID().toString(), taskId, arguments, WearBridgeContract.SDK_VERSION);
    }

    private TaskRequest(Parcel source) {
        requestId = Objects.requireNonNull(source.readString());
        taskId = Objects.requireNonNull(source.readString());
        Bundle value = source.readBundle(TaskRequest.class.getClassLoader());
        arguments = value == null ? Bundle.EMPTY : value;
        sdkVersion = source.readInt();
    }

    public String getRequestId() { return requestId; }
    public String getTaskId() { return taskId; }
    public Bundle getArguments() { return new Bundle(arguments); }
    public int getSdkVersion() { return sdkVersion; }

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(requestId);
        dest.writeString(taskId);
        dest.writeBundle(arguments);
        dest.writeInt(sdkVersion);
    }

    public static final Creator<TaskRequest> CREATOR = new Creator<>() {
        @Override public TaskRequest createFromParcel(Parcel source) { return new TaskRequest(source); }
        @Override public TaskRequest[] newArray(int size) { return new TaskRequest[size]; }
    };
}
