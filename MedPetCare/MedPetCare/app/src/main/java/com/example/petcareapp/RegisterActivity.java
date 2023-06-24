package com.example.petcareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcareapp.model.User;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "Tag";
    EditText rgNume, rgPrenume, rgEmail, rgPass;
    Button rgBtn;
    TextView existingUser;
    Spinner spinnerUser;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    private PreferenceUtils preferenceUtils;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        preferenceUtils = new PreferenceUtils(RegisterActivity.this);

        rgNume = findViewById(R.id.editTextRegisterNume);
        rgPrenume = findViewById(R.id.editTextPersonRegisterPrenume);
        rgEmail = findViewById(R.id.editTextEmailRegister);
        rgPass = findViewById(R.id.editTextRegisterPassword);
        rgBtn=findViewById(R.id.buttonLogin);
        existingUser = findViewById(R.id.tvNewUser);
        spinnerUser = findViewById(R.id.spinnerUserType);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        //verificam daca utilizatorul nu este deja logat
        if(fAuth.getCurrentUser() !=null){
            startActivity(new Intent (getApplicationContext(),MainActivity.class));
            finish();
        }

        rgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nume=rgNume.getText().toString();
                String prenume=rgPrenume.getText().toString();
                String email= rgEmail.getText().toString();
                String password=rgPass.getText().toString();
                String spinner =spinnerUser.getSelectedItem().toString();

                if (spinner.equals("Detin un animal de companie")){
                    spinner = "Utilizator";
                }else if (spinner.equals("Sunt medic veterinar")){
                    spinner = "Veterinar";
                }else {
                    spinner = "Utilizator";
                }

                if (nume.length() == 0 || prenume.length() == 0 || email.length() == 0 || password.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Nu ati introdus toate datele necesare", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(rgPass.length() < 6) {
                    rgPass.setError("Minim 6 caractere necesare");
                    return;
                }

                if (TextUtils.isEmpty(spinner)){
                    Toast.makeText(getApplicationContext(), "Vă rugăm să selectați tipul de utilizator", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                String finalSpinner = spinner;
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //trimitem email de verificare
                            FirebaseUser FirebaseUser = fAuth.getCurrentUser();
                            FirebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this, "Emailul de verificare a fost trimis", Toast.LENGTH_SHORT).show();

                                    userID = fAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = fStore.collection("users").document(userID);
                                    User user = new User();
                                    user.setUserId(userID);
                                    user.setUserType(finalSpinner);
                                    user.setEmail(email);
                                    user.setParola(password);
                                    user.setNume(nume);
                                    user.setPrenume(prenume);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Profilul utilizatorului a fost creat pentru" + userID);
                                            Toast.makeText(RegisterActivity.this,"Cont creat", Toast.LENGTH_SHORT).show();
                                            preferenceUtils.setString(Constants.PreferenceKeys.USER_ID, userID);
                                            if (finalSpinner.equals("Utilizator")){
                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Utilizator");
                                            }else if (finalSpinner.equals("Veterinar")){
                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Veterinar");
                                            }else {
                                                preferenceUtils.setString(Constants.PreferenceKeys.USER_TYPE, "Utilizator");
                                            }
                                            startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Emailul nu a fost trimis" + e.getMessage());
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this,"Eroare!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        existingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(this, R.array.userType, R.layout.spnr_usertype);
        adapter.setDropDownViewResource(R.layout.drpdb_usertype);
        spinnerUser.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}