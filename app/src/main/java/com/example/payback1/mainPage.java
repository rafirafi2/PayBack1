package com.example.payback1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mainPage extends AppCompatActivity {


    String loggedInEmail;

    ListView listView;

    Button addContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        addContact = findViewById(R.id.add_contact);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            loggedInEmail = user.getEmail();
            Log.d(TAG, "DocumentSnapshot data: " + loggedInEmail);
        } else {
            // No user is signed in
            Toast.makeText(mainPage.this, "Server Error!",
                    Toast.LENGTH_SHORT).show();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(loggedInEmail).collection("contacts").
                get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<String> contacts = new ArrayList<>();

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "DOCUMENTS: "+ document.getId() + " => " + document.getData());
                        contacts.add(document.getId());
                        Log.d(TAG, "DOCUMENTS: "+ contacts.toString());
                    }
                    listView = findViewById(R.id.listview);

                    ArrayAdapter arrayAdapter =new ArrayAdapter((Context)mainPage.this, android.R.layout.simple_list_item_1, contacts);
                    listView.setAdapter(arrayAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            String contactName = listView.getItemAtPosition(i).toString();

                            Intent intent = new Intent(getBaseContext(), contactPage.class);
                            intent.putExtra("contactName", contactName);
                            startActivity(intent);
                        }
                    });
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainPage.this);
                builder.setTitle("Add contact");


                // Set up the input
                final EditText input = new EditText(mainPage.this);
                // Specify the type of input expected; this, for example, sets string to input
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseFirestore data = FirebaseFirestore.getInstance();
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", input.getText().toString());
                        user.put("debt", 0);
                        user.put("credit", 0);

                        data.collection("users").document(loggedInEmail).collection("contacts").
                                document(input.getText().toString()).set(user);
                        data.collection("users").document(loggedInEmail).collection("contacts").
                                document("no contacts").delete();
                        finish();
                        startActivity(getIntent());
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
}