package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.skyapp.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        //init views
       // mProfileTv = findViewById(R.id.profileTv);
        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        actionBar.setTitle("SkyApp");
        HomeFragment f1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, f1,"");
        ft1.commit();
        checkUserStatus();
        }
     public void updateToken(String token){
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
         Token mToken = new Token(token);
         reference.child(mUid).setValue(mToken);
     }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private  BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
         @Override
         public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            //handle item click
             switch (menuItem.getItemId()){
                 case R.id.nav_home:
                     actionBar.setTitle("SkyApp");
                     HomeFragment f1 = new HomeFragment();
                     FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                     ft1.replace(R.id.content, f1,"");
                     ft1.commit();
                     return true;
                 case R.id.nav_profile:
                     actionBar.setTitle("Profile");
                     ProfileFragment f2 = new ProfileFragment();
                     FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                     ft2.replace(R.id.content, f2,"");
                     ft2.commit();
                     return true;
                 case R.id.nav_users:
                     actionBar.setTitle("Bạn bè");
                     UsersFragment f3 = new UsersFragment();
                     FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                     ft3.replace(R.id.content, f3,"");
                     ft3.commit();
                     return true;
                 case R.id.nav_add_post:
                     actionBar.setTitle("Tạo bài viết");
                     AddPostFragment f4 = new AddPostFragment();
                     FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                     ft4.replace(R.id.content, f4,"");
                     ft4.commit();
                     return true;
                 case R.id.nav_notification:
                     actionBar.setTitle("Thông Báo");
                     NotificationsFragment f5 = new NotificationsFragment();
                     FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                     ft5.replace(R.id.content, f5,"");
                     ft5.commit();
                     return true;
             }

             return false;
         }
     };
    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser() ;
        if (user != null){
             //user is signed in stay here
            //set email of logged in user
            // mProfileTv.setText(user.getEmail());
            mUid = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("current_USERID",mUid);
            editor.apply();
            //updateToken
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();

    }

}
    
