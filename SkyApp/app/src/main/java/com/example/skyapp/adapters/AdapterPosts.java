package com.example.skyapp.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skyapp.AddPostActivity;
import com.example.skyapp.PostDetailActivity;
import com.example.skyapp.R;
import com.example.skyapp.ThereProfileActivity;
import com.example.skyapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.skyapp.R.*;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLike = false ;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        final String uId = postList.get(i).getuId();
        String uName = postList.get(i).getuName();
        String uEmail = postList.get(i).getuEmail();
        String uDp = postList.get(i).getuDp();
        final String pId = postList.get(i).getpId();
        String pTitle = postList.get(i).getpTitle();
        String pDescription = postList.get(i).getpDescription();
        String pTimeStamp = postList.get(i).getpTime();
        final String pImage = postList.get(i).getpImage();
        String pLikes = postList.get(i).getpLikes();
        String pComments = postList.get(i).getpComments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pLikeTv.setText(pLikes+ " Likes " );
        myHolder.pCommentTv.setText(pComments+ " Comments");

        //set Likes for each post
        setLikes(myHolder,pId);

        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(drawable.ic_avatar).into(myHolder.uPictureIv);
        }
        catch (Exception e){

        }
        if (pImage.equals("noImage")){
            myHolder.pImageIv.setVisibility(View.GONE);
        }
        else {
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            //set image post
            try {
                Picasso.get().load(pImage).into(myHolder.pImageIv);
            } catch (Exception e) {

            }
        }
        //handle button click

        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(myHolder.moreBtn,uId,myUid,pId,pImage);
                }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;
                //get id of the post clicked
                final String postIde= postList.get(i).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike){
                            if (snapshot.child(postIde).hasChild(myUid)){
                                //already liked, so remove like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else {
                                //not liked, like it
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;

                                addToHisNotifications(""+uId,""+pId,"Liked your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);

            }
        });
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
            }
        });
        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uId",uId);
                context.startActivity(intent);
            }
        });
    }
    private  void addToHisNotifications(String hisUid, String pId,String message){
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<Object,String>hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("notification",message);
        hashMap.put("sUid",myUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(hisUid).child("Notification").child(timeStamp).setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, final String pId, final String pImage) {
        final PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        if (uId.equals(myUid)) {
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");
        //click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == 0) {
                    beginDelete(pId, pImage);
                }
                else if (i == 1) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if (i == 2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }
        else {
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView uPictureIv,pImageIv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikeTv,pCommentTv;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init View
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv = itemView.findViewById(R.id.pLikeTv);
            pCommentTv = itemView.findViewById(id.pCommentTv);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
