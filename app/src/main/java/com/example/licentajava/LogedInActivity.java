package com.example.licentajava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.licentajava.Model.Event;
import com.example.licentajava.Utils.FirebaseDatabaseHelper;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class LogedInActivity extends AppCompatActivity {
    private Button mLogoutButton;
    private Button mOwnedButton;
    private Button mAttendingButton;
    private FirebaseAuth mAuth;
    private Profile logedPorf;
    private TextView profileName;
    private CircleImageView circleImageView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.Adapter adapter2;
    private List<Event> list;
    private List<Event> listAttending;
    private List<Event> listManaging;
    private List<String> ownedEventsIds;
    MessagesClient mMessagesClient;
    String Status;
    FirebaseDatabaseHelper fire;
    private SwipeRefreshLayout swipeRefresh;


    MessageListener mMessageListener;
    Message mMessage;
    Message myMessage;

    String mName="";
    String mId="";
    String mEmail="";

   AccessToken accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loged_in);

        initInstance();

        loadEvents();
        initButtons();
        checkPermission();

        createMessageListener();
        fire=new FirebaseDatabaseHelper();



    }
    public void initInstance(){
        mLogoutButton=(Button) findViewById(R.id.log_out);
        mOwnedButton=(Button) findViewById(R.id.owned_events);
        mAttendingButton=(Button) findViewById(R.id.attending_events);
        profileName=(TextView) findViewById(R.id.profile_name);
        circleImageView=findViewById(R.id.profile_picture);
        logedPorf=Profile.getCurrentProfile();
        mAuth = FirebaseAuth.getInstance();
        accessToken=AccessToken.getCurrentAccessToken();
        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list=new ArrayList<>();
        listAttending=new ArrayList<>();
        listManaging=new ArrayList<>();
        ownedEventsIds=new ArrayList<>();
        mAttendingButton.setBackgroundColor(Color.BLUE);
        swipeRefresh=(SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        adapter2=new ListAdapter(listManaging,LogedInActivity.this,"");

    }
    public void initButtons(){
        mLogoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                updateUI();
            }
        });
        mOwnedButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                recyclerView.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
                mAttendingButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backround));
                mOwnedButton.setBackgroundColor(Color.BLUE);
            }
        });
        mAttendingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                mOwnedButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backround));
                mAttendingButton.setBackgroundColor(Color.BLUE);

            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                adapter.notifyDataSetChanged();
                adapter2.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            updateUI();
        }
        subscribe();




    }
    private void refreshData(){


        list.clear();
        listManaging.clear();
        listAttending.clear();

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try{

                            JSONObject evnts=object.getJSONObject("events");
                            JSONArray array=evnts.getJSONArray("data");
                            for(int i=0;i<array.length();i++) {
                                JSONObject o = array.getJSONObject(i);
                                JSONObject Loc=null;
                                if(o.has("place")) {
                                    Loc = o.getJSONObject("place");
                                }
                                String eventName="";
                                String eventId="";
                                String eventDescription="";
                                String locName="";
                                String startTime="";

                                if(o.has("name")) {
                                    eventName = o.getString("name");

                                }
                                if(o.has("id")) {
                                    eventId = o.getString("id");
                                }
                                if(o.has("description")) {
                                    eventDescription = o.getString("description");
                                }
                                if(o.has("start_time")) {
                                    startTime = o.getString("start_time");
                                }
                                if(Loc!=null){
                                    if (Loc.has("name")){
                                        locName=Loc.getString("name");
                                    }
                                }
                                Event item = new Event(
                                        eventId,
                                        eventName,
                                        eventDescription,
                                        locName,
                                        startTime

                                );

                                list.add(item);

                            }
                            filterEventsRefresh();

                        }

                        catch(JSONException e) {

                            Log.e("OFF",e.getMessage());
                        }


                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();

    }
    private void loadEvents(){
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading data....");
        progressDialog.show();


        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try{
                            progressDialog.dismiss();
                            String name="";
                            String id="";

                            if(object.has("name")) {
                                name = object.getString("name");
                                mName=name;
                            }

                            if(object.has("id")) {
                                id = object.getString("id");
                                mId=id;
                            }
                            Log.e("hatz",mName);
                            Log.e("NAmeeee",mId);
                            if(object.has("email")) {
                                mEmail = object.getString("email");
                            }
                            else{
                                mEmail="-";
                            }
                            Log.e("Emmaill",mEmail);
                            String image_url="https://graph.facebook.com/"+id+"/picture?type=normal";
                            profileName.setText(name);
                            Glide.with(getApplicationContext()).load(image_url).into(circleImageView);
                            JSONObject evnts=object.getJSONObject("events");
                            JSONArray array=evnts.getJSONArray("data");
                            for(int i=0;i<array.length();i++) {
                                JSONObject o = array.getJSONObject(i);
                                JSONObject Loc=null;
                                if(o.has("place")) {
                                     Loc = o.getJSONObject("place");
                                }
                                String eventName="";
                                String eventId="";
                                String eventDescription="";
                                String locName="";
                                String startTime="";

                                if(o.has("name")) {
                                    eventName = o.getString("name");

                                }
                                if(o.has("id")) {
                                    eventId = o.getString("id");
                                }
                                if(o.has("description")) {
                                    eventDescription = o.getString("description");
                                }
                                if(o.has("start_time")) {
                                    startTime = o.getString("start_time");
                                }
                                if(Loc!=null){
                                    if (Loc.has("name")){
                                        locName=Loc.getString("name");
                                    }
                                }
                                Event item = new Event(
                                        eventId,
                                        eventName,
                                        eventDescription,
                                        locName,
                                        startTime

                                );
                                list.add(item);

                                }
                            filterEvents();


                        }

                        catch(JSONException e) {

                            Log.e("OFF",e.getMessage());
                        }


                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events,name,id,email");
        request.setParameters(parameters);
        request.executeAsync();

    }
    private void filterEvents(){
        GraphRequest request2 = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            JSONObject evnts = object.getJSONObject("events");
                            JSONArray array = evnts.getJSONArray("data");
                            for(int j=0;j<list.size();j++){
                                boolean a=false;
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);
                                    String ownedID=o.getString("id");
                                    Log.d("FIRST-ID",list.get(j).getId());
                                    Log.d("Second-ID",ownedID);
                                    if(list.get(j).getId().equals(ownedID)){
                                        a=true;
                                    }

                                }
                                if(a){
                                    listManaging.add(list.get(j));
                                }
                                else{
                                    listAttending.add(list.get(j));
                                }
                            }
                            adapter=new OwnedListAdapter(listAttending,LogedInActivity.this);
                            adapter2=new ListAdapter(listManaging,LogedInActivity.this,mEmail);
                            recyclerView.setAdapter(adapter);
                            mOwnedButton.setEnabled(true);
                            mAttendingButton.setEnabled(true);

                        }
                        catch(JSONException e) {

                            Log.e("OFF",e.getMessage());
                        }

                    }

                });

        Bundle parameters2 = new Bundle();
        parameters2.putString("fields", "events{owner}");
        request2.setParameters(parameters2);
        request2.executeAsync();
    }
    private void filterEventsRefresh(){
        GraphRequest request2 = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            JSONObject evnts = object.getJSONObject("events");
                            JSONArray array = evnts.getJSONArray("data");
                            for(int j=0;j<list.size();j++){
                                boolean a=false;
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);
                                    String ownedID=o.getString("id");
                                    if(list.get(j).getId().equals(ownedID)){
                                        a=true;
                                    }

                                }
                                if(a){
                                    listManaging.add(list.get(j));
                                }
                                else{
                                    listAttending.add(list.get(j));
                                }
                            }
                            adapter.notifyDataSetChanged();
                            adapter2.notifyDataSetChanged();

                        }
                        catch(JSONException e) {

                            Log.e("OFF",e.getMessage());
                        }

                    }

                });

        Bundle parameters2 = new Bundle();
        parameters2.putString("fields", "events{owner}");
        request2.setParameters(parameters2);
        request2.executeAsync();

    }
    private void updateUI(){
        Toast.makeText(this, "You loged out", Toast.LENGTH_LONG).show();

        Intent accountIntent=new Intent(LogedInActivity.this,MainActivity.class);
        startActivity(accountIntent);
        finish();

    }
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMessagesClient = Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build());
        }
    }
    private void subscribe() {
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
    }
    private void createMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String recievedMessage = new String(message.getContent());
                try {
                    JSONObject eventul = new JSONObject(recievedMessage);
                    String eventId= eventul.getString("id");
                    String time=eventul.getString("time");
                    for(Event e: listAttending) {
                     if (eventId.equals(e.getId())) {
                     Toast.makeText(LogedInActivity.this, "You are attending: " + e.getName(),
                               Toast.LENGTH_SHORT).show();
                          fire.addOrUpdateData(eventId,mId,mName,mEmail,time);
                     }
                    }
                    Log.d("found", "Found message: " + new String(message.getContent()));
                }catch (Exception e){
                    Log.d("found", "Found message: " + new String(e.getMessage()));
                    Toast.makeText(LogedInActivity.this, "There is a problem in recieveing the message with the host",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLost(Message message) {
                Toast.makeText(LogedInActivity.this, "You left the event",
                        Toast.LENGTH_SHORT).show();
                Log.d("lost", "Lost sight of message: " + new String(message.getContent()));
            }
        };
    }
   @Override
    public  void onStop(){
       Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();

   }


}
