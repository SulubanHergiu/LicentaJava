package com.example.licentajava;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentajava.Model.Attendee;

import java.util.List;

public class AteendeeView_Config {
    private Context mContext;
    private attendeeAdapter mAttandeeAdapter;
    public void setConfig(RecyclerView recyclerView,Context context,List<Attendee> attendees,List<String> keys){
        mContext=context;
        mAttandeeAdapter= new attendeeAdapter(attendees,keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAttandeeAdapter);
    }

    class AttendeeItemView extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mEmail;
        private TextView mStart;
        private TextView mFinish;

        private String key;

        public AttendeeItemView(ViewGroup parent){
            super(LayoutInflater.from(mContext).inflate(R.layout.attendee_list_item, parent, false));
            mName=(TextView)  itemView.findViewById(R.id.attendeeName);
            mEmail=(TextView)  itemView.findViewById(R.id.attendeeEmail);
            mStart=(TextView)  itemView.findViewById(R.id.attendeeFirst);
            mFinish=(TextView)  itemView.findViewById(R.id.attendeeEnd);
        }
        public void bind(Attendee attendee,String key){
            mName.setText(attendee.getName());
            mEmail.setText(attendee.getEmail());
            mStart.setText(attendee.getStartTime());
            mFinish.setText(attendee.getEndTime());
            this.key= key;
        }

    }
    class attendeeAdapter extends  RecyclerView.Adapter<AttendeeItemView>{
        private List<Attendee> mAttendeeList;
        private List<String> mKeys;

        public attendeeAdapter(List<Attendee> mAttendeeList, List<String> mKeys) {
            this.mAttendeeList = mAttendeeList;
            this.mKeys = mKeys;
        }

        @NonNull
        @Override
        public AttendeeItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AttendeeItemView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AttendeeItemView holder, int position) {
            holder.bind(mAttendeeList.get(position),mKeys.get(position));
        }

        @Override
        public int getItemCount() {
            return mAttendeeList.size();
        }
    }
}
