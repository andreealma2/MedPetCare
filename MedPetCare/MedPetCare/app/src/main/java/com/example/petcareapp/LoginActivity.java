package com.example.petcareapp;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.utils.Constants;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    EditText edUsername, edPassword;
    Button btn;
    TextView tv, forgotTextLink;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    private PreferenceUtils preferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceUtils = new PreferenceUtils(LoginActivity.this);
        edUsername = findViewById(R.id.editTextEmailRegister);
        edPassword = findViewById(R.id.editTextRegisterPassword);
        btn = findViewById(R.id.buttonLogin);
        tv = findViewById(R.id.tvNewUser);
        progressBar = findViewById(R.id.progressBar2);
        fAuth = FirebaseAuth.getInstance();
        forgotTextLink = findViewById(R.id.textViewForgotPass);

        if (!TextUtils.isEmpty(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))){
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edUsername.getText().toString();
                String password = edPassword.getText().toString();
                if (username.length() == 0 || password.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Introduceti datele de logare", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //autentificare utilizator

                fAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // autentificare cu succes, se salveaza date utilizator
                                    FirebaseUser user = fAuth.getCurrentUser();

                                    // verificam daca adresa de email a fost verificata
                                    /*if (user != null && user.isEmailVerified()) {*/
                                        progressBar.setVisibility(View.GONE);
                                        // Email verificat, se poate autentifica utilizatorul
                                        preferenceUtils.setString(Constants.PreferenceKeys.USER_ID, user.getUid());
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference userRef = db.collection("users").document(user.getUid());

                                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        String userType = document.getString("userType");
                                                        // compatatie tipuri utilizatori
                                                        if (userType != null) {
                                                            if (userType.equals("Utilizator")) {
                                                                // utilizatorul este proprietar animal companie
                                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Utilizator");
                                                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                                                                finish();
                                                            } else if (userType.equals("Veterinar")) {
                                                                // utilizatorul este veterinar
                                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Veterinar");
                                                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                                                                finish();
                                                            } else {
                                                                // nu se stie tipul utilizatorului
                                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Utilizator");
                                                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                                                                finish();
                                                            }
                                                        } else {
                                                        }
                                                    } else {
                                                    }
                                                } else {
                                                }
                                            }
                                        });
                                    /*} else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "E-mailul nu este încă verificat !", Toast.LENGTH_SHORT).show();
                                    }*/
                                } else {
                                    // mesaj utilizator pentru autentificare esuata
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Autentificare esuata.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                /*fAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Inregistrare reusita", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Eroare!" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });*/
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

        forgotTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetmail = new EditText(view.getContext());
                resetmail.setHint("Email Address");
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Doriti sa resetati parola?");
                passwordResetDialog.setMessage("Introduceti parola pentru a primi linkul de resetare");
                passwordResetDialog.setView(resetmail);

                passwordResetDialog.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int witch) {
                        //extrag email si trimit link

                        String mail = resetmail.getText().toString();
                        if (!TextUtils.isEmpty(mail.trim())) {
                            fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(LoginActivity.this, "Linkul de resetare a fost trimis", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, "Eroare! Link de resetare netrimis" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Please Enter Valid Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //inchide dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}