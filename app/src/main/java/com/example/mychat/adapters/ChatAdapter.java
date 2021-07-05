package com.example.mychat.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychat.R;
import com.example.mychat.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    private static final int YOUR_MSG_TYPE = 0;
    private static final int MY_MSG_TYPE = 1;
    private List<Message> messages;
    Context context;


    FirebaseUser fUser;

    public ChatAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MY_MSG_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.my_message_item, parent, false);
            return new ChatViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.your_message_item, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(message.getTime()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, d MMM");
        String time = dateFormat.format(c.getTime());

        boolean isText = message.getImageUrl() == null;
        if(isText){
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        }
        else {
            holder.photoImageView.setVisibility(View.VISIBLE);
            holder.messageTextView.setVisibility(View.GONE);
            Glide.with(holder.photoImageView.getContext()).load(message.getImageUrl()).into(holder.photoImageView);
        }
        holder.timeTextView.setText(time);

        holder.linearLayoutChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });


        if(position == messages.size()-1){
            if(message.isSeen())
                holder.isSeenTextView.setText("seen");
            else
                holder.isSeenTextView.setText("delivered");

        }
        else{
            holder.isSeenTextView.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timeMessage = messages.get(position).getTime();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages");
        Query query = dbRef.orderByChild("time").equalTo(timeMessage);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myId)){
                        ds.getRef().removeValue();
                    }
                    else{
                        Toast.makeText(context, "You can delete only your messages", Toast.LENGTH_LONG).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messages.get(position).getSender().equals(fUser.getUid())){
            return MY_MSG_TYPE;
        }
        else{
            return YOUR_MSG_TYPE;
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImageView;
        private TextView messageTextView;
        private TextView timeTextView;
        private TextView isSeenTextView;

        private RelativeLayout linearLayoutChat;

        public ChatViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            messageTextView = itemView.findViewById(R.id.bubbleText);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            isSeenTextView = itemView.findViewById(R.id.isSeenTextView);
            linearLayoutChat = itemView.findViewById(R.id.layoutChat);
        }
    }

}
