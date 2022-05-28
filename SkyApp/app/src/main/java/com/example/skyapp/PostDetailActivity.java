package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skyapp.adapters.AdapterComments;
import com.example.skyapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    String myUid, myEmail, myName, myDp, postId,pLikes,hisUid,hisDp, hisName,pTitle,pDescription,pTimeStamp,pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    ProgressDialog pd;

    ImageView uPictureIv,pImageIv;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;
    TextView uNameTv,pTimeTv,  pTitleTv,pDescriptionTv,pLikeTv,pCommentTv;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    AdapterComments adapterComments;
    List<ModelComment> commentList;


    //add comment view
    ImageView cAvatarIv;
    ImageButton sendBtn;
    EditText commentEt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //Actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init view
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikeTv = findViewById(R.id.pLikeTv);
        pCommentTv = findViewById(R.id.pCommentTv);
        profileLayout = findViewById(R.id.profileLayout);

        recyclerView = findViewById(R.id.commentRecyclerView);

        cAvatarIv = findViewById(R.id.cAvatarIv);
        sendBtn = findViewById(R.id.sendBtn);
        commentEt = findViewById(R.id.commentEt);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        actionBar.setSubtitle("SignedIn as: "+myEmail);

        loadComments();

        //send comment btn click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }

        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
    }
    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        //init comment list
        commentList= new ArrayList<>();

        //path of the post, to get it's comment
        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);
                    //
                    adapterComments = new AdapterComments(getApplicationContext(),commentList,myUid,postId);

                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void likePost() {
        mProcessLike = true;

        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike){
                    if (snapshot.child(postId).hasChild(myUid)){
                        //already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    }

                    else {
                        //not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;
                        addToHisNotifications(""+hisUid,""+postId,"Liked Your Post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    private void showMoreOptions() {
        final PopupMenu popupMenu = new PopupMenu(this,moreBtn, Gravity.END);

        if (hisUid.equals(myUid)) {
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }

        //click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == 0) {
                    beginDelete();
                }
                else if (i == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage")){
            deleteWithoutImage();
        }
        else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding Comment....");

        //get data from comment edit text
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "comment is empty....", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //each other will have a child "Comments" the will contain comments of that post
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uName",myName);
        hashMap.put("uDp",myDp);
        //put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Add....", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                        addToHisNotifications(""+hisUid,""+postId,"Commented on your post");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateCommentCount() {
        //whenever user adds comment increase the comment count as we did for like count
        mProcessComment = true;
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    dbRef.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {

        Query mRef = FirebaseDatabase.getInstance().getReference("Users");
        mRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    myName =""+ds.child("name").getValue();
                    myDp =""+ds.child("image").getValue();
                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_avatar).into(cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_avatar).into(cAvatarIv);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    pTitle = ""+ds.child("pTitle").getValue();
                    pDescription = ""+ds.child("pDescription").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    hisUid= ""+ds.child("uId").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    String CommentCount = ""+ds.child("pComments").getValue();
                    //convert timestamp to dd/mm/yy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                    //set Data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescription);
                    pLikeTv.setText(pLikes + " Likes");
                    pTimeTv.setText(pTime);
                    pCommentTv.setText(CommentCount+ " Comments");
                    uNameTv.setText(hisName);


                    if (pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {
                        pImageIv.setVisibility(View.VISIBLE);
                        //set image post
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_avatar).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_avatar).into(uPictureIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!= null){
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide some menu items
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_chat).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
