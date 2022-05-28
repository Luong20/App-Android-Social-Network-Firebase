package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.skyapp.adapters.AdapterChatlist;
import com.example.skyapp.models.ModelChat;
import com.example.skyapp.models.ModelChatlist;
import com.example.skyapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListChatActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.chatlist_recyclerView);
        chatlistList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers users = ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist: chatlistList){
                        if (users.getUid() != null && users.getUid().equals(chatlist.getId())){
                            usersList.add(users);
                            break;
                        }
                    }
                    adapterChatlist = new AdapterChatlist(ListChatActivity.this,usersList);
                    //set Adapter
                    recyclerView.setAdapter(adapterChatlist);
                    //set last Message
                    for (int i=0; i<usersList.size();i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String thelastMessage = "default" ;
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat == null){
                        continue;
                    }
                    String sender = modelChat.getSender();
                    String receiver = modelChat.getReceiver();
                    if (sender == null||receiver == null){
                        continue;
                    }
                    if (modelChat.getReceiver().equals(currentUser.getUid())&&modelChat.getSender().equals(uid)||
                            modelChat.getReceiver().equals(uid) && modelChat.getSender().equals(currentUser.getUid())){
                        thelastMessage = modelChat.getMessage();
                    }
                }
                adapterChatlist.setLastMessageMap(uid,thelastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
