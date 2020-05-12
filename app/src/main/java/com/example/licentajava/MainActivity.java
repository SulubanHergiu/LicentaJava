package com.example.licentajava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CallbackManager mCallbackManager;

    private static final String Tag="Facelog";

    private FirebaseAuth mAuth;
    private Button mFacebookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
       mFacebookButton =(Button) findViewById(R.id.login_button);
       mFacebookButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mFacebookButton.setEnabled(false);
               LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile","user_events"));
               LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                   @Override
                   public void onSuccess(LoginResult loginResult) {

                       Log.d(Tag, "facebook:onSuccess:" + loginResult);
                       handleFacebookAccessToken(loginResult.getAccessToken());
                   }

                   @Override
                   public void onCancel() {
                       Log.d(Tag, "facebook:onCancel");
                       // ...
                   }

                   @Override
                   public void onError(FacebookException error) {
                       Log.d(Tag, "facebook:onError", error);
                       // ...
                   }
               });
           }
       });

        }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            updateUI();
        }

    }
    private void updateUI(){
        Toast.makeText(this, "You are loged in", Toast.LENGTH_LONG).show();

        Intent accountIntent=new Intent(MainActivity.this,LogedInActivity.class);
        startActivity(accountIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Tag,"signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mFacebookButton.setEnabled(true);
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Tag, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mFacebookButton.setEnabled(true);

                        }
                    }
                });
    }

    }

