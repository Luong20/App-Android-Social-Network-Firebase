package com.example.skyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.SearchView;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.chatlist_recyclerView);
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
        return  view;
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
                    adapterChatlist = new AdapterChatlist(getContext(),usersList);
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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    /*inflating option menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        //inflating menu
        menuInflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_chat).setVisible(false);

        super.onCreateOptionsMenu(menu, menuInflater);
    }


    /* handle menu item click*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
