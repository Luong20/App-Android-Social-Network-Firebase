package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    ProgressDialog progressDialog;


    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tạo tài khoản");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang Đăng ký ...");

        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email=  mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error edit and focus to email edittext
                    mEmailEt.setError("Email không hợp lệ");
                    mEmailEt.setFocusable(true);

                }
                else if (password.length()<6){
                    //set error edit and focus to password  edittext
                    mPasswordEt.setError("Độ dài mật khẩu không ít hơn 6 ký tự");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email,password);//register user
                }
            }


        });
        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }
    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                           //Get user email and uid from auth
                            String email = user.getEmail();
                            String uid =  user.getUid();
                            //when user is registered store user info in firebase realtime database too
                            //using HashMap
                            HashMap<Object, String> hashMap =new HashMap<>();
                            //put info in hash map
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name","");
                            hashMap.put("onlineStatus","online");
                            hashMap.put("typingTo","noOne");
                            hashMap.put("phone","");
                            hashMap.put("image","");
                            hashMap.put("cover","");

                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference reference = firebaseDatabase.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Đăng ký...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Quá trình xác thực đã thất bại.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}
