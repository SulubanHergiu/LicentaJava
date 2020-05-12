package com.example.licentajava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


    MessageListener mMessageListener;
    Message mMessage;
    Message myMessage;

   AccessToken accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loged_in);

        mLogoutButton=(Button) findViewById(R.id.log_out);
        mOwnedButton=(Button) findViewById(R.id.owned_events);
        mAttendingButton=(Button) findViewById(R.id.attending_events);
        profileName=(TextView) findViewById(R.id.profile_name);
        circleImageView=findViewById(R.id.profile_picture);
        logedPorf=Profile.getCurrentProfile();
        mAuth = FirebaseAuth.getInstance();
        accessToken=AccessToken.getCurrentAccessToken();
        getWindow().setStatusBarColor(Color.WHITE);
        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list=new ArrayList<>();
        listAttending=new ArrayList<>();
        listManaging=new ArrayList<>();
        ownedEventsIds=new ArrayList<>();
        mAttendingButton.setBackgroundColor(Color.BLUE);


        loadEvents();

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
                adapter2=new ListAdapter(listManaging,LogedInActivity.this);
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


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            updateUI();
        }



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
                            String name=object.getString("name");
                            String id=object.getString("id");
                            String image_url="https://graph.facebook.com/"+id+"/picture?type=normal";
                            profileName.setText(name);
                            Glide.with(getApplicationContext()).load(image_url).into(circleImageView);
                            JSONObject evnts=object.getJSONObject("events");
                            JSONArray array=evnts.getJSONArray("data");
                            for(int i=0;i<array.length();i++) {
                                JSONObject o = array.getJSONObject(i);
                                JSONObject Loc = o.getJSONObject("place");
                                int thisPosition = i;
                                Event item = new Event(
                                        o.getString("id"),
                                        o.getString("name"),
                                        o.getString("description"),
                                        Loc.getString("name"),
                                        o.getString("start_time")

                                );
                                list.add(item);

                                }


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
                                                    Log.e("FIRST-ID",list.get(j).getId());
                                                    Log.e("Second-ID",ownedID);
                                                        if(list.get(j).getId().equals(ownedID)){
                                                            a=true;
                                                        }

                                                    }
                                                    if(a==true){
                                                        listManaging.add(list.get(j));
                                                    }
                                                    else{
                                                        listAttending.add(list.get(j));
                                                    }
                                                }
                                                adapter=new OwnedListAdapter(listAttending,LogedInActivity.this);
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

                        catch(JSONException e) {

                            Log.e("OFF",e.getMessage());
                        }


                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events,name,id");
        request.setParameters(parameters);
        request.executeAsync();

    }
    private void updateUI(){
        Toast.makeText(this, "You loged out", Toast.LENGTH_LONG).show();

        Intent accountIntent=new Intent(LogedInActivity.this,MainActivity.class);
        startActivity(accountIntent);
        finish();

    }

   @Override
    public  void onStop(){
        super.onStop();


   }
}
