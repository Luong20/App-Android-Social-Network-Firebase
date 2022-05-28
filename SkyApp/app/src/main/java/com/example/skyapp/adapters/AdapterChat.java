package com.example.skyapp.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skyapp.R;
import com.example.skyapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

   private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    FirebaseUser fUser;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        String message = chatList.get(i).getMessage();
        String timeStamp= chatList.get(i).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        holder.timeTv.setText(dateTime);

        holder.messageTv.setText(message);

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message ? ");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(i);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });


        try {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_avatar).into(holder.profileIv);
        }
        catch (Exception e){

        }
        if (i == chatList.size()-1){
            if (chatList.get(i).isSeen()){
                holder.isSeenTv.setText("Xem");
            }
            else {
                holder.isSeenTv.setText("Chưa Xem");
            }
        }
        else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int i) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp =chatList.get(i).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if (ds.child("sender").getValue().equals(myUID)){
//                        ds.getRef().removeValue();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message","this message was deleted...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message delete...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context,"you can delete only your messag",Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {

            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
