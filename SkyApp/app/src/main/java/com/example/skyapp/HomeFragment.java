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
import android.widget.Toast;

import com.example.skyapp.adapters.AdapterPosts;
import com.example.skyapp.models.ModelPost;
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
public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.postRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first , for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);
        //init post list
        postList = new ArrayList<>();
        LoadPost();
        return view;

    }

    private void LoadPost() {
        //path of all post
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void SearchPosts(final String search){
        //path of all post
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle().toLowerCase().contains(search.toLowerCase())||
                            modelPost.getpDescription().toLowerCase().contains(search.toLowerCase())){
                        postList.add(modelPost);
                    }


                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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

        //searchview to search post by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s))
                {
                    SearchPosts(s);
                }
                else {
                    LoadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s))
                {
                    SearchPosts(s);
                }
                else {
                    LoadPost();
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
