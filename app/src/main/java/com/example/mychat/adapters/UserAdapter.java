package com.example.mychat.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.R;
import com.example.mychat.activities.DrawerActivity;
import com.example.mychat.models.Message;
import com.example.mychat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;
    private OnUserClickListener listener;
    private Context context;
    User currentUser;

    public UserAdapter(ArrayList<User> users, Context context){
        this.users = users;
        this.context = context;
    }

    public interface OnUserClickListener{
        void onUserClick(int position);
    }
    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        UserViewHolder userViewHolder = new UserViewHolder(view, listener);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        currentUser = users.get(position);
        try {
            if(!currentUser.getAvatarMockUpResource().equals(" ")){
                Uri image = Uri.parse(currentUser.getAvatarMockUpResource());
                if(image.toString().contains("%2Fstorage"))
                    Picasso.get().load(image).rotate(270).into(holder.avatarImageV);
                else
                    Picasso.get().load(image).rotate(90).into(holder.avatarImageV);
            }
            else
                Picasso.get().load(R.drawable.user_image).into(holder.avatarImageV);

        }catch (Exception e){
            Picasso.get().load(R.drawable.user_image).into(holder.avatarImageV);
        }
        holder.userNameTextV.setText(currentUser.getName());
        String onlineStatus = currentUser.getOnlineStatus();
        if(onlineStatus.equals("online"))
            holder.lastSeenTextV.setText(onlineStatus);
        else{
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(onlineStatus));
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, d MMM");
            String time = dateFormat.format(c.getTime());
            holder.lastSeenTextV.setText("last seen at " + time);
        }

        if(context.getClass().equals(DrawerActivity.class)) {
            holder.countMessTextV.setVisibility(View.VISIBLE);
            holder.messTextV.setVisibility(View.VISIBLE);
            holder.timeDialogTextV.setVisibility(View.VISIBLE);
        }
        else
            holder.lastSeenTextV.setVisibility(View.VISIBLE);

        DatabaseReference messRef = FirebaseDatabase.getInstance().getReference().child("messages");
        messRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int count = 0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    if(message.getRecipient().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())&&
                    message.getSender().equals(currentUser.getId())){
                        if(!message.isSeen()){
                            count++;
                        }
                    }
                    String text;
                    if(message.getRecipient().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())&&
                            message.getSender().equals(currentUser.getId()) ||
                            message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())&&
                                    message.getRecipient().equals(currentUser.getId())){
                        if(message.getText().length()>52){
                            text = message.getText().substring(0, 51)+"...";
                            holder.messTextV.setText(text);
                        }
                        else
                            holder.messTextV.setText(message.getText());
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(Long.parseLong(message.getTime()));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                        String time = dateFormat.format(c.getTime());
                        holder.timeDialogTextV.setText(time);
                    }
                }
                if(count != 0){
                    holder.countMessTextV.setText(count+"");
                }
                else
                    holder.countMessTextV.setText("");

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }

        });

        /*holder.layoutUser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this dialog?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentUser.setAccountStatus("deleted");
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
        });*/
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarImageV;
        private TextView userNameTextV, messTextV, countMessTextV, timeDialogTextV, lastSeenTextV;
        //private RelativeLayout layoutUser;

        public UserViewHolder(@NonNull View itemView, OnUserClickListener listener) {
            super(itemView);
            avatarImageV = itemView.findViewById(R.id.avatarImageV);
            userNameTextV = itemView.findViewById(R.id.userNameTextV);
            messTextV = itemView.findViewById(R.id.messTV);
            countMessTextV = itemView.findViewById(R.id.countMessTV);
            //layoutUser = itemView.findViewById(R.id.layoutUser);
            timeDialogTextV = itemView.findViewById(R.id.timeDialogTV);
            lastSeenTextV = itemView.findViewById(R.id.lastSeenTV);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onUserClick(position);
                        }
                    }
                }
            });

        }
    }
}
