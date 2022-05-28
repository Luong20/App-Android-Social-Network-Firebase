package com.example.skyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN =100 ;
    GoogleSignInClient mGoogleSignInClient;

    Button mLoginBtn;
    EditText mEmailEt,mPasswordEt;
    TextView notHaveAccountTv, mRecoverPassTv;

    SignInButton mGoogleLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //Progress dialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Đăng Nhập");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        notHaveAccountTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);


        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);


        mAuth = FirebaseAuth.getInstance();



        //login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //input data
                String email = mEmailEt.getText().toString();
                String password = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                   //invalid email patterns set error
                    mEmailEt.setError("Email không hợp lệ");
                    mEmailEt.setFocusable(true);
                }
                else {
                    //valid email patterns
                    loginUser(email,password);
                }
            }

        });
        //google login
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        //not have account textview
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
        //init progress
        progressDialog = new ProgressDialog(this);

    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục mật khẩu");
        //Set layout linear layout
        LinearLayout linearLayout =new LinearLayout(this);
        //view to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("khôi phục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email= emailEt.getText().toString().trim();
                beginRecover(email);
            }
        });
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });
        //showDialog
        builder.create().show();
    }

    private void beginRecover(String email) {
        //show progress dialog
        progressDialog.setMessage("Đang gửi email ...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email đã được gửi" , Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Thất bại..", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            //get and show proper error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        //show progress dialog
        progressDialog.setMessage("Đăng nhập...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress dialog
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //user is logged in , so start LoginActivity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            //dismiss progress dialog
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Quá trình xác thực thất bại",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
                progressDialog.dismiss();
                //error, get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
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
                            }

                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại....", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

