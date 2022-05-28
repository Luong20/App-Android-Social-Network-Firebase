package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skyapp.adapters.AdapterPosts;
import com.example.skyapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postsRecyclerView;

    FirebaseAuth firebaseAuth;

    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        postsRecyclerView = findViewById(R.id.recyclerView_post);
        //init view
        avatarIv =findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);

        Intent intent = getIntent();
        uId = intent.getStringExtra("uId");

        postList = new ArrayList<>();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_avatar).into(avatarIv);
                    }
                    catch (Exception e){
                    }
                    try {
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkUserStatus();
        loadHisPost();


    }

    private void loadHisPost() {
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first , for this last
        layoutManager.setStackFromEnd(true);
        layoutManager.setStackFromEnd(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("uId").equalTo(uId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this,postList);

                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void SearchHisPost(final String search){
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //show newest post first , for this last
        layoutManager.setStackFromEnd(true);
        layoutManager.setStackFromEnd(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("uId").equalTo(uId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (modelPost.getpTitle().toLowerCase().contains(search.toLowerCase())||
                            modelPost.getpDescription().toLowerCase().contains(search.toLowerCase())){
                        postList.add(modelPost);}

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this,postList);

                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser() ;
        if (user != null){
            //user is signed in stay here
            //set email of logged in user
            // mProfileTv.setText(user.getEmail());
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
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
        menu.findItem(R.id.action_chat).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s))
                {
                    SearchHisPost(s);
                }
                else {
                    loadHisPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!TextUtils.isEmpty(s))
                {
                    SearchHisPost(s);
                }
                else {
                    loadHisPost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
