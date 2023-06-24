package com.example.petcareapp;

import static com.example.petcareapp.RegisterActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    ImageView btnDashboard;
    TextView  logout, saveProfile;
    EditText numeProfilEdit, prenumeProfilEdit;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnDashboard = findViewById(R.id.tvDasboardBack);
        logout = findViewById(R.id.tvToolbarLogout);
        numeProfilEdit = findViewById(R.id.numeProfilEdit);
        prenumeProfilEdit = findViewById(R.id.prenumeProfilEdit);
        saveProfile = findViewById(R.id.saveProfile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = mAuth.getCurrentUser().getUid();
                String newName = numeProfilEdit.getText().toString();
                String newSurname = prenumeProfilEdit.getText().toString();
                updateUserDisplayName(userId, newName, newSurname);
                startActivity(new Intent(getApplicationContext(),DashboardActivity.class));

            }
        });


        btnDashboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(EditProfile.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

        logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAuth.signOut();
                        Intent intent = new Intent(EditProfile.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }

    private void updateUserDisplayName(String userId, String newName, String newSurname) {
        DocumentReference userRef = fStore.collection("users").document(userId);

        if (newName.length() == 0 || newSurname.length() == 0) {
            Toast.makeText(getApplicationContext(), "Nu ati introdus toate datele necesare", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Succes", Toast.LENGTH_SHORT).show();


        userRef.update("nume", newName, "prenume", newSurname)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfile.this, "Profilul utilizatorului a fost actualizat", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfile.this, "Eroare", Toast.LENGTH_SHORT).show();
                            Log.e("EditActivity", "Eroare modificare informatii", task.getException());
                        }
                    }
                });
        }
    }

}