package com.example.licentajava;
import android.os.Build;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

public class DeviceMessage {
    private static final Gson gson = new Gson();

    private final String mInstanceId;
    private final String mMessageBody;
    private final String mMessageContent;


    public static Message newNearbyMessage(String instanceId) {
        DeviceMessage deviceMessage = new DeviceMessage(instanceId);
        return new Message(gson.toJson(deviceMessage).getBytes(Charset.forName("UTF-8")));
    }

    public static Message newNearbyMessage(String instanceId, String messageContent) {
        DeviceMessage deviceMessage = new DeviceMessage(instanceId, messageContent);
        return new Message(gson.toJson(deviceMessage).getBytes(Charset.forName("UTF-8")));
    }


    public static DeviceMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                DeviceMessage.class);
    }

    private DeviceMessage(String instanceId) {
        this.mInstanceId = instanceId;
        this.mMessageBody = Build.MODEL;
        this.mMessageContent = "default";
    }

    private DeviceMessage(String instanceId, String messageContent) {
        this.mInstanceId = instanceId;
        this.mMessageBody = Build.MODEL;
        this.mMessageContent = messageContent;
    }

    protected String getMessageBody() {
        if (mMessageContent != null) {
            return mMessageBody + ", " + getMessageContent();
        }
        return mMessageBody;
    }

    protected String getMessageContent() {
        return mMessageContent;
    }
}
