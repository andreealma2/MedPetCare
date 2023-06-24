package com.example.petcareapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petcareapp.ui.chat.UserListActivity;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    Button btnLogout;
    ImageView account,chat, imgPets;
    private CardView cardPets, cardChat, cardAlert, cardConsultation;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private PreferenceUtils preferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        preferenceUtils = new PreferenceUtils(DashboardActivity.this);

        btnLogout = findViewById(R.id.btnLogoutDashboard);
        account = findViewById(R.id.imgViewSetari);
        chat = findViewById(R.id.imgChat);
        imgPets = findViewById(R.id.imgPets);
        cardPets = findViewById(R.id.cardPets);
        cardChat = findViewById(R.id.cardChat);
        cardAlert = findViewById(R.id.cardAlert);
        cardConsultation = findViewById(R.id.cardConsultation);
        mAuth = FirebaseAuth.getInstance();


        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        cardChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });

        cardAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, AlertActivity.class);
                startActivity(intent);
            }
        });

        cardConsultation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ConsultationActivity.class);
                startActivity(intent);
            }
        });

        cardPets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, Register_Pet.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                preferenceUtils.clearAll();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}