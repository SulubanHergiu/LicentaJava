package com.example.licentajava;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.licentajava.Model.Event;
import com.example.licentajava.Utils.FirebaseDatabaseHelper;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Event> events;
    private final Context context;
    boolean isSeding=false;
    private  Message  mMessage;
    private boolean run=true;
    boolean alreadySent=false;
    int a=0;
    String mail;
    FirebaseDatabaseHelper mFire;



    public ListAdapter(List<Event> events, Context context,String mail) {
        this.events = events;
        this.context=context;
        this.mFire=new FirebaseDatabaseHelper();
        this.mail=mail;
    }

    @Override

    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.textViewName.setText(event.getName());
        holder.textViewDescr.setText(event.getDescription());
        holder.textViewDate.setText(event.getStart_time());
        holder.textViewLocation.setText(event.getPlace());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createEventDialog(event);
            }
        });
    }

    public void createEventDialog(Event event){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_event);
        dialog.setTitle(event.getName());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //Nearby.getMessagesClient(context).publish(mMessage);
        Button messageButton = (Button) dialog.findViewById(R.id.message_button);
        Button emailButton = (Button) dialog.findViewById(R.id.email_button);
        Button exitButton = (Button) dialog.findViewById(R.id.exit_button);
        TextView name = (TextView) dialog.findViewById(R.id.eventName);
        TextView date = (TextView) dialog.findViewById(R.id.eventDate);
        TextView location = (TextView) dialog.findViewById(R.id.eventLocation);
        name.setText(event.getName());
        date.setText(event.getStart_time());
        location.setText(event.getPlace());
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSeding) {
                    run = true;
                    final Handler handler = new Handler();
                    Timer timer = new Timer();
                    final TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        if (run) {
                                            if (alreadySent) {
                                                Nearby.getMessagesClient(context).unpublish(mMessage);
                                                isSeding = false;
                                            }
                                            Date currentTime = Calendar.getInstance().getTime();
                                            String currentTimeString = currentTime.toString();
                                            JSONObject eventData = new JSONObject();
                                            try {
                                                eventData.put("id", event.getId());
                                                eventData.put("time", currentTimeString);
                                                mMessage = new Message(eventData.toString().getBytes());
                                                Toast.makeText(context, eventData.toString(), Toast.LENGTH_SHORT).show();
                                                Nearby.getMessagesClient(context).publish(mMessage);
                                                alreadySent = true;
                                                isSeding = true;
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(context, "There is an error in sending the message.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });

                        }
                    };
                    timer.schedule(task, 0, 30000);
                    messageButton.setText("STOP");
                    messageButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_backround_red));
                }
                else{
                    messageButton.setText("Make attendance");
                    messageButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_backround));
                    isSeding=false;
                    Nearby.getMessagesClient(context).unpublish(mMessage);
                    run=false;
                    alreadySent=false;

                }

            }


        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSeding){
                    isSeding=false;
                    Nearby.getMessagesClient(context).unpublish(mMessage);
                }
                run=false;
                alreadySent=false;
                dialog.dismiss();
            }
        });
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSeding){
                    isSeding=false;
                    Nearby.getMessagesClient(context).unpublish(mMessage);
                }
                run=false;
                alreadySent=false;
                dialog.dismiss();
                Intent intent=new Intent(context, AttendeeListActivity.class);
                //intent.putExtra("name",event.getName());
                intent.putExtra("name",event.getName());
                intent.putExtra("id",event.getId());
                intent.putExtra("mail",mail);
                context.startActivity(intent);
            }
        });

        dialog.show();


}


    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewName;
        public TextView textViewDescr;
        public TextView textViewDate;
        public TextView textViewLocation;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewName=(TextView) itemView.findViewById(R.id.eventName);
            textViewDate=(TextView) itemView.findViewById(R.id.eventDate);
            textViewDescr=(TextView) itemView.findViewById(R.id.eventDescription);
            textViewLocation=(TextView) itemView.findViewById(R.id.eventLocation);
            linearLayout=(LinearLayout) itemView.findViewById(R.id.eventListItem);
        }
    }

}
