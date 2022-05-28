package com.example.skyapp;

        import android.Manifest;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.ProgressDialog;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.net.Uri;
        import android.os.Bundle;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;
        import androidx.core.view.MenuItemCompat;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import android.provider.MediaStore;
        import android.text.TextUtils;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.SearchView;
        import android.widget.Switch;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.skyapp.adapters.AdapterPosts;
        import com.example.skyapp.models.ModelPost;
        import com.google.android.gms.auth.api.signin.internal.Storage;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;
        import com.google.android.material.floatingactionbutton.FloatingActionButton;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Query;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.storage.StorageReference;
        import com.google.firebase.storage.UploadTask;
        import com.squareup.picasso.Picasso;

        import java.security.Key;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;

        import static android.app.Activity.RESULT_OK;
        import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    FloatingActionButton fab;

    RecyclerView postsRecyclerView;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;

    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri;

    String profileOrCoverPhoto;

    StorageReference storageReference;
    String storagePath ="User_Profile_Cover_Imgs/";

    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    String uId;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
        //init view
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        postList = new ArrayList<>();
        postsRecyclerView = view.findViewById(R.id.recyclerView_post);

        pd= new ProgressDialog(getActivity());

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
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
                        Picasso.get().load(cover).placeholder(R.drawable.ic_add_img).into(coverIv);
                    }
                    catch (Exception e){
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first , for this last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
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

                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void searchMyPosts(final String search) {
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first , for this last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
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
                        postList.add(modelPost);
                    }
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean resultl = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && resultl;
    }
    private void requestCameraPermission(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }


    private void showEditProfileDialog() {
        String options[]={"Thay đổi ảnh đại diện","Thay dổi ảnh bìa","Sửa Tên","Sửa số điện thoại"};
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Chỉnh sửa");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //edit profile click
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePickdialog();

                }
                else if (which == 1){
                    //edit cover click
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                    showImagePickdialog();
                }
                else if (which == 2){
                    //edit name click
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                else if (which == 3){
                    //edit phone click
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Chỉnh sửa "+ key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText = new EditText(getActivity());
        editText.setHint(""+ key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key , value);

                    databaseReference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Cập nhật...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    if (key.equals("name")){
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("uId").equalTo(uId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //update name  in current users comments on post
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")) {
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uId);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds:snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Please enter "+key , Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showImagePickdialog() {
        String options[]={"Camera","Thự viện ảnh"};
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Chọn ảnh từ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //camera click
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }

                }
                else if (which == 1){
                    //Gallery click
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }

            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && WriteStorageAccepted){
                        pickFromCamera ();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length >0){

                    boolean WriteStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if ( WriteStorageAccepted){
                        pickFromGallery ();

                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){

                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();

        String filePathAndName = storagePath +""+ profileOrCoverPhoto + "_" + firebaseUser.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(profileOrCoverPhoto,downloadUri.toString());
                    databaseReference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Image Update...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                }
                            });
                    if (profileOrCoverPhoto.equals("image")){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("uId").equalTo(uId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //update user image in current users comments on posts
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")) {
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uId);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds:snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                    }
                }
                else {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values =  new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri =getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser() ;
        if (user != null){
            //user is signed in stay here
            //set email of logged in user
            // mProfileTv.setText(user.getEmail());
            uId = user.getUid();
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
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }
                else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }
                else {
                    loadMyPosts();
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
