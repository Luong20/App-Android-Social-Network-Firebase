package com.example.skyapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skyapp.PostDetailActivity;
import com.example.skyapp.R;
import com.example.skyapp.models.ModelNotification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.MyHolder>{
   private Context context;
    private ArrayList<ModelNotification> notificationsList;
    FirebaseAuth firebaseAuth;

    public AdapterNotifications(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        final ModelNotification model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        final String timeStamp = model.getTimeStamp();
        String image = model.getsImage();
        String senderUid = model.getsUid();
        final String pId= model.getpId();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        final String time = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //get name, email, image of user of notification from hisUid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds:snapshot.getChildren()){
                        String name= ""+ds.child("name").getValue();
                        String image = ""+ds.child("image").getValue();
                        String email = ""+ds.child("email").getValue();
                        model.setsName(name);
                        model.setsEmail(email);
                        model.setsImage(image);

                        holder.nameTv.setText(name);

                        try {
                            Picasso.get().load(image).placeholder(R.drawable.ic_avatar).into(holder.avatarIv);
                        }
                        catch (Exception e){
                            holder.avatarIv.setImageResource(R.drawable.ic_avatar);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            })
        ;

        holder.timeTv.setText(time);
        holder.notificationsTv.setText(notification);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa");
                builder.setMessage("Bạn có chắc muốn xóa thông báo này");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notification").child(timeStamp).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Thông báo đã được xóa....", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;
        TextView nameTv, notificationsTv,timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            notificationsTv = itemView.findViewById(R.id.notificationTv);
        }
    }
}
