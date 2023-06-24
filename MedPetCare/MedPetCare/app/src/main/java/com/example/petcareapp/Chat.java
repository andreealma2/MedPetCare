package com.example.petcareapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Chat extends AppCompatActivity {

    Button btn2Logout;
    ImageView btnDashboard; //sageata back catre Dashnboard
    TextView logout; //logout text din toolbar
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btn2Logout = findViewById(R.id.btn2logout);
        mAuth = FirebaseAuth.getInstance();
        btnDashboard = findViewById(R.id.tvDasboardBack);
        logout = findViewById(R.id.tvToolbarLogout);

        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Chat.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(Chat.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(Chat.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}