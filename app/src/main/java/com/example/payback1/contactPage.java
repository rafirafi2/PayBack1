package com.example.payback1;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class contactPage extends AppCompatActivity {

    String loggedInEmail, contactName;

    TextView getDebt, getCredit, name, totalDiff;
    Button addDebt, addCredit, backToContacts, reset;
    ImageView profilePic;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_page);


        //get current user email to get their contacts list from firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            loggedInEmail = user.getEmail();
            Log.d(TAG, "DocumentSnapshot data: " + loggedInEmail);
        } else {
            // No user is signed in
            Toast.makeText(contactPage.this, "Server Error!",
                    Toast.LENGTH_SHORT).show();
        }

        getDebt = findViewById(R.id.getDebt);
        getCredit = findViewById(R.id.getCredit);
        addDebt = findViewById(R.id.add_debt);
        addCredit = findViewById(R.id.add_credit);
        backToContacts = findViewById(R.id.backToContacts);
        name = findViewById(R.id.name);
        totalDiff = findViewById(R.id.totalDiff);
        profilePic = findViewById(R.id.profilePic);
        reset = findViewById(R.id.reset);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DocumentReference docRef = db.collection("users").document(loggedInEmail).collection("contacts").document(contactName);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                FirebaseFirestore data = FirebaseFirestore.getInstance();
                                Map<String, Object> user = new HashMap<>();
                                user.put("debt", 0);
                                user.put("credit", 0);


                                data.collection("users").document(loggedInEmail).collection("contacts").
                                        document(contactName).update(user);
                                finish();
                                startActivity(getIntent());

                            }
                        }
                    }
                });
            }
        });



        //back to contacts
        backToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), mainPage.class);
                startActivity(intent);
                finish();
            }
        });


        //get contact name from contact list page
        contactName = getIntent().getStringExtra("contactName");
        name.setText(contactName);

        //get image from storage if user has an profile pic saved from before
        StorageReference checkPic = storageRef.child(loggedInEmail).child(contactName);

        try {
            File localFile = File.createTempFile("tempfile",".png");
            checkPic.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Picasso.get().load(localFile).into(profilePic);
                }
            });

        }catch (IOException e){
            e.printStackTrace();
        }
        //end profile picture get from database


        //save image uri and call method to save image to cloud storage
        ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Here, no request code
                            Intent data = result.getData();
                            assert data != null;
                            Uri image = data.getData();
                            profilePic.setImageURI(image);
                            uploadImageToFirebaseStorage(image);
                        }
                    }
                });


        //open gallery and get image uri
        profilePic.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(intent);
        });







        //get profile contact debt and credit from firestore
        DocumentReference docRef = db.collection("users").document(loggedInEmail).collection("contacts").document(contactName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long debt = (Long) document.get("debt");
                        long credit = (Long) document.get("credit");
                        long diff = 0;

                        //checking difference for who owes money based on credit or debt being higher
                        if(debt>credit){
                            diff = debt-credit;
                            totalDiff.setText(contactName + " owes me a total of: " + Long.toString(diff));
                        }else if(credit>debt){
                            diff = credit-debt;
                            totalDiff.setText("I owe "+ contactName+" a total of: "+ Long.toString(diff));
                        }else if(credit==debt){
                            totalDiff.setText(contactName+ " and I are even!");
                        }

                        getDebt.setText("I owe "+ contactName+" : "+ Long.toString(credit));
                        getCredit.setText(contactName+" owes me : "+ Long.toString(debt));

                    }
                }
            }
        });


        //adds debt that the contact owes you
        addDebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(contactPage.this);
                builder.setTitle("Add how much "+ contactName + " owes you: ");


                // Set up the input
                final EditText input = new EditText(contactPage.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {




                        DocumentReference docRef = db.collection("users").document(loggedInEmail).collection("contacts").document(contactName);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {

                                        long debt = (Long) document.get("debt");
                                        String value = input.getText().toString();
                                        long toAdd = Long.parseLong(value) + debt;

                                        FirebaseFirestore data = FirebaseFirestore.getInstance();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("debt", toAdd);


                                        data.collection("users").document(loggedInEmail).collection("contacts").
                                                document(contactName).update(user);
                                        finish();
                                        startActivity(getIntent());

                                    }
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

        //adds credit that you owe the contact
        addCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(contactPage.this);
                builder.setTitle("Add how much you owe "+ contactName + " : ");


                // Set up the input
                final EditText input = new EditText(contactPage.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DocumentReference docRef = db.collection("users").document(loggedInEmail).collection("contacts").document(contactName);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {

                                        long credit = (Long) document.get("credit");
                                        String value = input.getText().toString();
                                        long toAdd = Long.parseLong(value) + credit;

                                        FirebaseFirestore data = FirebaseFirestore.getInstance();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("credit", toAdd);


                                        data.collection("users").document(loggedInEmail).collection("contacts").
                                                document(contactName).update(user);
                                        finish();
                                        startActivity(getIntent());

                                    }
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });


    }



    private void uploadImageToFirebaseStorage(Uri image){


        StorageReference fileRef = storageRef.child(loggedInEmail).child(contactName);
        fileRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(contactPage.this, "Image Uploaded",
                        Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(contactPage.this, "Image Failed to Upload",
                        Toast.LENGTH_SHORT).show();

            }
        });



    }


}