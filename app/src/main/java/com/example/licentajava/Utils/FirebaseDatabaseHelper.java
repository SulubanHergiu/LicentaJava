package com.example.licentajava.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.licentajava.Model.Attendee;
import com.example.licentajava.Model.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceAttendees;
    private List<Attendee> attendees= new ArrayList<>();
    boolean swi;

    public  FirebaseDatabaseHelper(){
        mDatabase=FirebaseDatabase.getInstance();
        //mReferenceAttendees=mDatabase.getReference("Attendee");

    }
    public interface  DataStatus{
        void DataIsLoaded(List<Attendee> attendees,List<String> keys);

    }
    public void readAttendees(String idEvent,final DataStatus dataStatus){
        mReferenceAttendees = mDatabase.getReference().child("Attendee").child(idEvent);
        mReferenceAttendees.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    attendees.clear();
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                            keys.add(keyNode.getKey());
                            Attendee attendee=keyNode.getValue(Attendee.class);
                            attendees.add(attendee);
                        }
                        dataStatus.DataIsLoaded(attendees,keys);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("ERROR",databaseError.getMessage());
            }
        });
    }
    public void addOrUpdateData(String eventId,String userId,String Name,String Email,String time){
        boolean result;
        DatabaseReference  referenceToUser = mDatabase.getReference().child("Attendee").child(eventId).child(userId);
        referenceToUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    referenceToUser.child("endTime").setValue(time);
                }
                else{
                    Attendee a= new Attendee(Name,Email,time,time);
                    referenceToUser.setValue(a);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            });



    }


   // public void addEvent(Event event){
   //     mReferenceEvents.child(event.getId()).setValue(event);
   // }
}
