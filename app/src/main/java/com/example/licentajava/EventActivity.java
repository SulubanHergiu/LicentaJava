package com.example.licentajava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;

public class EventActivity extends AppCompatActivity {
    private TextView name;
    private TextView description;
    private TextView time;
    private TextView location;

    private String eventName;
    private String eventId;
    private String eventDescription;
    private String userStatus;
    private String eventLocation;
    private String eventTime;
    private Button mMessageButton;
    private Button mEmailButton;
    private String Status;
    MessagesClient mMessagesClient;
    Boolean Sending=Boolean.FALSE;

    MessageListener mMessageListener;
    Message mMessage;
    Message myMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Intent eventIntent=getIntent();
        name=(TextView) findViewById(R.id.eventName);
        time=(TextView) findViewById(R.id.eventDate);
        description=(TextView) findViewById(R.id.eventDescription);
        location=(TextView) findViewById(R.id.eventLocation);
        mMessageButton=(Button)  findViewById(R.id.message_button);
        mEmailButton=(Button)  findViewById(R.id.email_button);
        eventName=eventIntent.getStringExtra("name");
        eventId=eventIntent.getStringExtra("id");
        eventLocation=eventIntent.getStringExtra("location");
        eventTime=eventIntent.getStringExtra("time");
        eventDescription=eventIntent.getStringExtra("description");
        userStatus=eventIntent.getStringExtra("status");
        name.setText(eventName);
        time.setText(eventTime);
        description.setText(eventDescription);
        location.setText(eventLocation);


        mMessageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mMessage = new Message(eventName.getBytes());
                Nearby.getMessagesClient(EventActivity.this).publish(mMessage);
                Sending=Boolean.TRUE;


            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMessagesClient = Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build());
        }

        createMessageListener();
        if (userStatus.equals("Attending")){
            mMessageButton.setEnabled(false);
            mMessageButton.setVisibility(View.GONE);
        }




    }
    @Override
    public void onStart() {
        super.onStart();

        subscribe();


    }
    private void subscribe() {
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
    }
    private void createMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String recievedMessage = new String(message.getContent());

                if (recievedMessage.equals(eventName)) {
                    Toast.makeText(EventActivity.this, "Do you allow the host to get the info for: " + recievedMessage,
                            Toast.LENGTH_SHORT).show();
                }
                Log.d("found", "Found message: " + new String(message.getContent()));
            }

            @Override
            public void onLost(Message message) {
                Toast.makeText(EventActivity.this, "You left the "+  new String(message.getContent())+ " event",
                        Toast.LENGTH_SHORT).show();
                Log.d("lost", "Lost sight of message: " + new String(message.getContent()));
            }
        };
    }
    @Override
    public void onStop(){
        if(Sending==Boolean.TRUE) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
        }
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }

}
