package com.example.skyapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.skyapp.adapters.AdapterNotifications;
import com.example.skyapp.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    RecyclerView notificationRv;
    FirebaseAuth firebaseAuth;
    private AdapterNotifications adapterNotifications;
    private ArrayList<ModelNotification> notificationsList;
    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationRv =view.findViewById(R.id.notificationRv);
        firebaseAuth = FirebaseAuth.getInstance();
        getNotifications();

        return view;
    }

    private void getNotifications() {
        notificationsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Notification")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelNotification model = ds.getValue(ModelNotification.class);
                            notificationsList.add(model);
                        }
                        //adapter
                        adapterNotifications = new AdapterNotifications(getActivity(),notificationsList);
                        //set to Rv
                        notificationRv.setAdapter(adapterNotifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
