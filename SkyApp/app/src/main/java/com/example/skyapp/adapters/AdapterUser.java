package com.example.skyapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.skyapp.ChatActivity;
import com.example.skyapp.ThereProfileActivity;
import com.example.skyapp.models.ModelUsers;
import com.example.skyapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{
    Context context;
    List<ModelUsers> usersList;


    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        final String hisUid = usersList.get(i).getUid();
        String userImage= usersList.get(i).getImage();
        String userName= usersList.get(i).getName();
        final String userEmail= usersList.get(i).getEmail();

        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);

        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_avatar)
                    .into(myHolder.mAvatarTv);
        }
        catch (Exception e){

        }
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile","Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //profile click
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uId",hisUid);
                            context.startActivity(intent);
                        }
                        if (which==1){
                            //chat click
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra( "hisUid", hisUid);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatarTv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mAvatarTv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
