package com.example.licentajava;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.licentajava.Model.Event;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Event> events;
    private final Context context;

    public ListAdapter(List<Event> events, Context context) {
        this.events = events;
        this.context=context;
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

                Intent  eventIntent = new Intent(context, EventActivity.class);
                eventIntent.putExtra("id",event.getId());
                eventIntent.putExtra("status","Owned");
                eventIntent.putExtra("description",event.getDescription());
                eventIntent.putExtra("name",event.getName());
                eventIntent.putExtra("time",event.getStart_time());
                eventIntent.putExtra("location",event.getPlace());
                context.startActivity(eventIntent);
            }
        });
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
