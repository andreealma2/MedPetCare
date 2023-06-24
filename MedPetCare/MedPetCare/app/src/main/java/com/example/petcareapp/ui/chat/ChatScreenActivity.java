package com.example.petcareapp.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.petcareapp.R;
import com.example.petcareapp.adapters.MessageAdapter;
import com.example.petcareapp.model.Message;
import com.example.petcareapp.utils.Constants;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatScreenActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private ListView messageListView;
    private MessageAdapter messageAdapter;

    private FirebaseFirestore firestore;
    private ListenerRegistration messageListener;

    private String currentUserId;
    private String chatUserId;
    private PreferenceUtils preferenceUtils;
    private ImageButton btnBack;
    private TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        preferenceUtils = new PreferenceUtils(ChatScreenActivity.this);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);

        String selectedUsername = getIntent().getStringExtra("chatUserName");
        tvUserName.setText(selectedUsername);


        currentUserId = preferenceUtils.getString(Constants.PreferenceKeys.USER_ID);
        chatUserId = getIntent().getStringExtra("chatUserId");

        messageAdapter = new MessageAdapter(this);
        messageListView.setAdapter(messageAdapter);

        firestore = FirebaseFirestore.getInstance();
        btnBack.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                }
            }
        });

        startListeningForMessages();

        // Marcheaza mesajele ca citite cand utilizatorul se intoarce la tabela mesaje
        messageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    markMessagesAsRead();
                }
            }
        });
    }

    private void markMessagesAsRead() {
        List<Message> messages = messageAdapter.getMessages();
        for (Message message : messages) {
            if (!message.isCurrentUser() && !message.isRead()) {
                // Uactualizeaza mesajele ca citite in baza de date
                firestore.collection("messages").document(message.getMessageId())
                        .update("isRead", true)
                        .addOnSuccessListener(aVoid -> {
                            // actualizeaza mesaj local
                            message.setRead(true);
                            messageAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }
    }

    private void sendMessage(String messageText) {
        Map<String, Object> message = new HashMap<>();
        message.put("sender", currentUserId);
        message.put("receiver", chatUserId);
        message.put("message", messageText);
        message.put("isRead", false);
        message.put("time", new Date()); // adaugare timestamp

        firestore.collection("messages").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String itemId = documentReference.getId();
                message.put("messageId", itemId);
                documentReference.set(message);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Firestore", "Eroare", e);
            }
        });
        messageEditText.setText("");
    }

    private void startListeningForMessages() {
        messageListener = firestore.collection("messages")
                .orderBy("time", Query.Direction.DESCENDING) // Ordoneaza mesajele in functie de timestamp
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    List<Message> newMessages = new ArrayList<>();

                    for (DocumentChange dc : snapshot.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String sender = dc.getDocument().getString("sender");
                            String receiver = dc.getDocument().getString("receiver");
                            String message = dc.getDocument().getString("message");
                            String messageId = dc.getDocument().getString("messageId");
                            boolean isRead = Boolean.TRUE.equals(dc.getDocument().getBoolean("isRead"));
                            Date time = dc.getDocument().getDate("time"); // Get timestamp

                            if (sender.equals(currentUserId) && receiver.equals(chatUserId)) {
                                newMessages.add(new Message(sender, message, time, true, isRead, messageId));
                            } else if (sender.equals(chatUserId) && receiver.equals(currentUserId)) {
                                newMessages.add(new Message(sender, message, time, false, isRead, messageId));
                            }
                        }
                    }

                    // Adauga mesaje in ordine inversa, cel mai recent apare primul
                    Collections.reverse(newMessages);

                    for (Message message : newMessages) {
                        messageAdapter.addMessage(message);
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}

