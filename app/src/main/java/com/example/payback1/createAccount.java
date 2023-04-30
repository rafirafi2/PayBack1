package com.example.payback1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class createAccount extends AppCompatActivity {

    FirebaseAuth mAuth;

    EditText email, username, password, confirmPass;

    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        email =findViewById(R.id.enterEmail);
        username =findViewById(R.id.enterUsername);
        password =findViewById(R.id.enterPassword);
        confirmPass =findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.sign_in_button);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email1, username1, password1, confirmPassword;


                email1 = String.valueOf(email.getText());
                username1 = String.valueOf(username.getText());
                password1 = String.valueOf(password.getText());
                confirmPassword = String.valueOf(confirmPass.getText());

                if(TextUtils.isEmpty(email1)){
                    Toast.makeText(createAccount.this,"Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(username1)){
                    Toast.makeText(createAccount.this,"Enter Username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password1)){
                    Toast.makeText(createAccount.this,"Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(createAccount.this,"Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!confirmPassword.equals(password1)){
                    Toast.makeText(createAccount.this,"Enter Matching passwords", Toast.LENGTH_SHORT).show();
                    return;
                }



                mAuth.createUserWithEmailAndPassword(email1, password1)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(createAccount.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();

                                    FirebaseFirestore data = FirebaseFirestore.getInstance();

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username1);

                                    data.collection("users").document(email1).set(user);
                                    data.collection("users").document(email1).collection("contacts").
                                            document("no contacts").set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "successful!");
                                        }
                                    });



                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(createAccount.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });

    }
}