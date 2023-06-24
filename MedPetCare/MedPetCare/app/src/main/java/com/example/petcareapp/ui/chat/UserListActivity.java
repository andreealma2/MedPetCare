package com.example.petcareapp.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.petcareapp.R;
import com.example.petcareapp.adapters.UserAdapter;
import com.example.petcareapp.model.User;
import com.example.petcareapp.utils.Constants;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private ListView userListView;
    private UserAdapter userAdapter;

    private FirebaseFirestore firestore;

    private String currentUserId;
    private ImageButton btnBack;
    private PreferenceUtils preferenceUtils;
    private MaterialAutoCompleteTextView etUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        preferenceUtils = new PreferenceUtils(UserListActivity.this);

        userListView = findViewById(R.id.userListView);
        etUserType = findViewById(R.id.etUserType);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        final String[] userTypes = {"Veterinar", "Utilizator"};
        etUserType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etUserType.showDropDown();
                return false;
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                userTypes);
        etUserType.setAdapter(adapter);

        etUserType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = (String) parent.getItemAtPosition(position);
                if (selectedUser.equals("Utilizator")){
                    fetchUsers("Utilizator");
                }else {
                    fetchUsers("Veterinar");
                }
            }
        });


        currentUserId = preferenceUtils.getString(Constants.PreferenceKeys.USER_ID);

        userAdapter = new UserAdapter(this);
        userListView.setAdapter(userAdapter);

        firestore = FirebaseFirestore.getInstance();

        fetchUsers("Veterinar");

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = userAdapter.getItem(position);
                openChatActivity(user.getUserId(), user.getNume());
            }
        });
    }

    private void fetchUsers(String userType) {
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            if (!userId.equals(currentUserId)) {
                                if (userType.equals(document.getString("userType"))) {
                                    User user = new User();
                                    user.setUserId(userId);
                                    user.setUserType(document.getString("userType"));
                                    user.setEmail(document.getString("email"));
                                    user.setParola(document.getString("parola"));
                                    user.setNume(document.getString("nume"));
                                    user.setPrenume(document.getString("prenume"));
                                    user.setUnreadCount(getUnreadMessageCount(userId));
                                    users.add(user);
                                }
                            }
                        }
                        userAdapter.setUsers(users);
                    }
                });
    }

    private int getUnreadMessageCount(String userId) {
        final int[] unreadCount = {0};

        firestore.collection("messages")
                .whereEqualTo("sender", userId)
                .whereEqualTo("receiver", preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .whereEqualTo("isRead", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            unreadCount[0]++;
                        }
                        // Update the unread count in the corresponding user object
                        Log.e("onFailure", unreadCount[0] + " -- " + userId);
                        updateUnreadCount(userId, unreadCount[0]);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("onFailure", e.getMessage() + " **");
                    }
                });

        return unreadCount[0];
    }

    private void updateUnreadCount(String userId, int unreadCount) {
        // Gaseste corespondent user object in useradapter
        for (User user : userAdapter.getUsers()) {
            if (user.getUserId().equals(userId)) {
                user.setUnreadCount(unreadCount);
                break;
            }
        }
        // Notificare data modificata
        userAdapter.notifyDataSetChanged();
    }

    private void openChatActivity(String chatUserId, String userName) {
        Intent intent = new Intent(UserListActivity.this, ChatScreenActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        intent.putExtra("chatUserName", userName);
        intent.putExtra("chatUserId", chatUserId);
        startActivity(intent);
    }
}
