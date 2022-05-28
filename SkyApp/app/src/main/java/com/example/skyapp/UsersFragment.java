package com.example.skyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.skyapp.adapters.AdapterUser;
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
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser adapterUsers;
    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;



    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();

       recyclerView = view.findViewById(R.id.users_recyclerView);

       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

       usersList = new ArrayList<>();

       getAllUsers();

        return view;
    }

    private void getAllUsers() {
         final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }
                    adapterUsers  = new AdapterUser(getActivity(), usersList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchUser(final String query) {
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);

                        }

                    }
                    adapterUsers  = new AdapterUser(getActivity(), usersList);
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        //search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUser(s);
                }
                else {
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUser(s);
                }
                else {
                    getAllUsers();
                }

                return false;
            }
        });

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
        if(id == R.id.action_chat){
            startActivity(new Intent(getActivity(),ListChatActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
