package com.zbd.wearbridge.protocol;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public final class TaskResult implements Parcelable {
    private final String requestId;
    private final int status;
    private final Bundle data;
    private final String message;
    private final long durationMillis;

    public TaskResult(String requestId, int status, Bundle data, String message, long durationMillis) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.status = status;
        this.data = data == null ? Bundle.EMPTY : new Bundle(data);
        this.message = message == null ? "" : message;
        this.durationMillis = Math.max(0, durationMillis);
    }

    public static TaskResult error(TaskRequest request, int status, String message) {
        return new TaskResult(request.getRequestId(), status, Bundle.EMPTY, message, 0);
    }

    private TaskResult(Parcel source) {
        requestId = Objects.requireNonNull(source.readString());
        status = source.readInt();
        Bundle value = source.readBundle(TaskResult.class.getClassLoader());
        data = value == null ? Bundle.EMPTY : value;
        message = Objects.requireNonNullElse(source.readString(), "");
        durationMillis = source.readLong();
    }

    public String getRequestId() { return requestId; }
    public int getStatus() { return status; }
    public Bundle getData() { return new Bundle(data); }
    public String getMessage() { return message; }
    public long getDurationMillis() { return durationMillis; }
    public boolean isSuccess() { return status == TaskStatus.OK; }

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(requestId);
        dest.writeInt(status);
        dest.writeBundle(data);
        dest.writeString(message);
        dest.writeLong(durationMillis);
    }

    public static final Creator<TaskResult> CREATOR = new Creator<>() {
        @Override public TaskResult createFromParcel(Parcel source) { return new TaskResult(source); }
        @Override public TaskResult[] newArray(int size) { return new TaskResult[size]; }
    };
}
