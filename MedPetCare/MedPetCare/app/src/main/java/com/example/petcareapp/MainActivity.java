package com.example.petcareapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class MainActivity extends AppCompatActivity {

 ImageView btnDashboard;
FirebaseAuth mAuth;
FirebaseFirestore fStore;
String userId;
TextView nume, prenume, adresaEmail, logout, editProfile, changePass, numePrenUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nume = findViewById(R.id.numeProfilEdit);
        prenume = findViewById(R.id.prenumeProfilEdit);
        adresaEmail = findViewById(R.id.emailProfil);
        btnDashboard = findViewById(R.id.tvDasboardBack);
        editProfile = findViewById(R.id.editProfile);
        changePass = findViewById(R.id.changePass);
        numePrenUser = findViewById(R.id.numePrenUser);


        mAuth = FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        DocumentReference documentReference =fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                nume.setText(documentSnapshot.getString("nume"));
                prenume.setText(documentSnapshot.getString("prenume"));
                adresaEmail.setText(documentSnapshot.getString("email"));
                numePrenUser.setText(documentSnapshot.getString("userType"));
            }
        });


        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditProfile.class);
                startActivity(intent);
                finish();
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              EditText resetmail = new EditText(view.getContext());
                                              AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                                              passwordResetDialog.setTitle("Doriti sa resetati parola?");
                                              passwordResetDialog.setMessage("Introduceti parola pentru a primi linkul de resetare");
                                              passwordResetDialog.setView(resetmail);

                                              passwordResetDialog.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialogInterface, int witch) {
                                                      //extrag email si trimit link

                                                      String mail = resetmail.getText().toString();
                                                      mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                          @Override
                                                          public void onSuccess(Void unused) {
                                                              Toast.makeText(MainActivity.this, "Linkul de resetare a fost trimis", Toast.LENGTH_SHORT).show();

                                                          }
                                                      }).addOnFailureListener(new OnFailureListener() {
                                                          @Override
                                                          public void onFailure(@NonNull Exception e) {
                                                              Toast.makeText(MainActivity.this, "Eroare! Link de resetare netrimis" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                          }
                                                      });
                                                  }
                                              });
                                          }
                                      });

        logout = findViewById(R.id.tvToolbarLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}


