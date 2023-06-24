package com.example.petcareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcareapp.adapters.ConsultationsAdapter;
import com.example.petcareapp.adapters.PetsAdapter;
import com.example.petcareapp.model.Alerts;
import com.example.petcareapp.model.Consultation;
import com.example.petcareapp.model.Pet;
import com.example.petcareapp.model.User;
import com.example.petcareapp.utils.Constants;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConsultationActivity extends AppCompatActivity implements ConsultationsAdapter.OnItemLongClickListener {

    private ImageButton btnBack;
    private FirebaseFirestore dbPets;
    private TextView empty_view;
    private ConsultationsAdapter consultationsAdapter;
    private RecyclerView rvConsultation;
    private PreferenceUtils preferenceUtils;
    private String[] users;
    private String[] pets;
    private List<User> userList = new ArrayList<>();
    private List<Pet> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation);
        preferenceUtils = new PreferenceUtils(ConsultationActivity.this);

        // Initialize Firebase Firestore
        dbPets = FirebaseFirestore.getInstance();

        rvConsultation = findViewById(R.id.rvConsultation);
        empty_view = findViewById(R.id.empty_view);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        consultationsAdapter = new ConsultationsAdapter(ConsultationActivity.this);
        consultationsAdapter.setOnItemLongClickListener(this);
        rvConsultation.setHasFixedSize(true);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvConsultation.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        rvConsultation.addItemDecoration(divider);
        rvConsultation.setAdapter(consultationsAdapter);

        FloatingActionButton fab = findViewById(R.id.addConsultation);
        if (preferenceUtils.getString(Constants.PreferenceKeys.USER_TYPE).equals("Veterinar")) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addConsultation();
                }
            });
        } else {
            fab.setVisibility(View.GONE);
        }


        getUserNamesFromFirestore();

        if (preferenceUtils.getString(Constants.PreferenceKeys.USER_TYPE).equals("Veterinar")) {
            // Fetch items from Firestore and update RecyclerView
            fetchConsultationFromFirestore();
        }else {
            fetchConsultationFromFirestoreForUser();
        }
    }

    private void fetchConsultationFromFirestoreForUser() {
        dbPets.collection("Consultations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Consultation> itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Consultation consultation = document.toObject(Consultation.class);
                                if (consultation.getUserId().equals(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))) {
                                    itemList.add(consultation);
                                }
                            }
                            consultationsAdapter.setItems(itemList);
                            if (itemList.isEmpty()) {
                                empty_view.setVisibility(View.VISIBLE);
                            } else {
                                empty_view.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Firestore", "Eroare: ", task.getException());
                        }
                    }
                });
    }

    private void getUserNamesFromFirestore() {
        dbPets.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> userNameList = new ArrayList<>();
                        userList.clear();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String userId = documentSnapshot.getId();
                            String userName = documentSnapshot.getString("nume");
                            if (!userId.equals(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))) {
                                User user = documentSnapshot.toObject(User.class);
                                userNameList.add(userName);
                                userList.add(user);
                            }
                        }

                        users = userNameList.toArray(new String[0]);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting user list", e);
                    }
                });
    }

    private void fetchConsultationFromFirestore() {
        dbPets.collection("Consultations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Consultation> itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Consultation consultation = document.toObject(Consultation.class);
                                itemList.add(consultation);
                            }
                            consultationsAdapter.setItems(itemList);
                            if (itemList.isEmpty()) {
                                empty_view.setVisibility(View.VISIBLE);
                            } else {
                                empty_view.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void addConsultation() {

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_consultation, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultationActivity.this);
        builder.setCancelable(false);

        TextInputEditText etConsultationDate = view.findViewById(R.id.etConsultationDate);
        MaterialAutoCompleteTextView etUserName = view.findViewById(R.id.etUserName);
        MaterialAutoCompleteTextView etPetName = view.findViewById(R.id.etPetName);
        TextInputEditText etPetType = view.findViewById(R.id.etPetType);
        TextInputEditText etPetBreed = view.findViewById(R.id.etPetBreed);

        etUserName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etUserName.showDropDown();
                return false;
            }
        });

        etPetName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etPetName.showDropDown();
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                users);
        etUserName.setAdapter(adapter);

        final String[] selectedUserID = new String[1];
        final String[] selectedPetId = new String[1];
        etUserName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = (String) parent.getItemAtPosition(position);
                selectedUserID[0] = userList.get(position).getUserId();
                dbPets.collection("registerPets")
                        .document(userList.get(position).getUserId())
                        .collection("pets")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> petNameList = new ArrayList<>();
                                petList.clear();

                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String petName = documentSnapshot.getString("name");
                                    Pet pet = documentSnapshot.toObject(Pet.class);
                                    petNameList.add(petName);
                                    petList.add(pet);
                                }

                                pets = petNameList.toArray(new String[0]);

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ConsultationActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        pets);
                                etPetName.setAdapter(adapter);
                            }
                        });
            }
        });
        etPetName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPetId[0] = petList.get(position).getPetId();
                etPetType.setText(petList.get(position).getType());
                etPetBreed.setText(petList.get(position).getBreed());
            }
        });

        final Date[] _selectedDateTime = new Date[1];
        etConsultationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ConsultationActivity.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(ConsultationActivity.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        _selectedDateTime[0] = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        etConsultationDate.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        builder.setTitle(R.string.add_consultations)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAutoCompleteTextView etUserName = view.findViewById(R.id.etUserName);
                MaterialAutoCompleteTextView etPetName = view.findViewById(R.id.etPetName);
                TextInputEditText etPetType = view.findViewById(R.id.etPetType);
                TextInputEditText etPetBreed = view.findViewById(R.id.etPetBreed);
                TextInputEditText etSubject = view.findViewById(R.id.etSubject);
                TextInputEditText etRecommendation = view.findViewById(R.id.etRecommendation);
                TextInputEditText etConsultationDate = view.findViewById(R.id.etConsultationDate);

                if (TextUtils.isEmpty(etUserName.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Seletati numele utilizatorului!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetName.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati nume animal!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetType.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Seletati tip animal!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetBreed.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati rasa animal!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etSubject.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti subiect!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etRecommendation.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti recomandare!", Toast.LENGTH_SHORT).show();
                }  else if (TextUtils.isEmpty(etConsultationDate.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti data consultatiei!", Toast.LENGTH_SHORT).show();
                } else {
                    Consultation consultation = new Consultation(etUserName.getText().toString(), selectedUserID[0], etPetName.getText().toString(),
                            etPetType.getText().toString(), etPetBreed.getText().toString(), selectedPetId[0],
                            etSubject.getText().toString(), etRecommendation.getText().toString(), _selectedDateTime[0]);
                   addConsultationsToFirestore(consultation);
                    dialog.dismiss();
                }
            }
        });
    }

    private void addConsultationsToFirestore(Consultation consultation) {
        dbPets.collection("Consultations")
                .add(consultation)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String itemId = documentReference.getId();  // Retrieve the item ID
                        Log.d("Firestore", "Consultatie adaugata: " + itemId);

                        // actualizare petId cu item ID
                        consultation.setConsultationId(itemId);

                        // actualizare document Firestore cu item id
                        documentReference.set(consultation)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "Consultatie adaugata " + itemId);

                                        // Fetch items again to reflect the changes
                                        fetchConsultationFromFirestore();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Eroare adaugare consultatie", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare adaugare consultatie", e);
                    }
                });
    }
    @Override
    public void onItemLongClick(int position) {
        if (preferenceUtils.getString(Constants.PreferenceKeys.USER_TYPE).equals("Veterinar")){
            Consultation consultation = consultationsAdapter.getConsultation(position);
            showOptionsDialog(consultation);
        }
    }
    private void showOptionsDialog(final Consultation consultation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Optiuni");

        CharSequence[] options = {"Editeaza", "Sterge"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showEditDialog(consultation);
                } else if (which == 1) {
                    deleteItem(consultation);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteItem(Consultation consultation) {
        dbPets.collection("Consultations")
                .document(consultation.getConsultationId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Item deleted");
                        // folosim fetch pentru a vizualiza ultimele modificari
                        fetchConsultationFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error deleting item", e);
                    }
                });
    }
    private void showEditDialog(final Consultation consultation) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_consultation, null);
        final MaterialAutoCompleteTextView etUserName = view.findViewById(R.id.etUserName);
        etUserName.setText(consultation.getUserName());
        final MaterialAutoCompleteTextView etPetName = view.findViewById(R.id.etPetName);
        etPetName.setText(consultation.getPetName());
        final TextInputEditText etPetType = view.findViewById(R.id.etPetType);
        etPetType.setText(consultation.getPetType());
        final TextInputEditText etPetBreed = view.findViewById(R.id.etPetBreed);
        etPetBreed.setText(consultation.getPetBreed());
        final TextInputEditText etSubject = view.findViewById(R.id.etSubject);
        etSubject.setText(consultation.getSubject());
        final TextInputEditText etRecommendation = view.findViewById(R.id.etRecommendation);
        etRecommendation.setText(consultation.getRecommendation());
        final TextInputEditText etConsultationDate = view.findViewById(R.id.etConsultationDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String selectedDateTime = sdf.format(consultation.getConsultationDate());
        etConsultationDate.setText(selectedDateTime);

        etUserName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etUserName.showDropDown();
                return false;
            }
        });

        etPetName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etPetName.showDropDown();
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                users);
        etUserName.setAdapter(adapter);

        final String[] selectedUserID = new String[1];
        final String[] selectedPetId = new String[1];
        selectedPetId[0] = consultation.getPetId();
        selectedUserID[0] = consultation.getUserId();

        etUserName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = (String) parent.getItemAtPosition(position);
                selectedUserID[0] = userList.get(position).getUserId();
                dbPets.collection("registerPets")
                        .document(userList.get(position).getUserId())
                        .collection("pets")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> petNameList = new ArrayList<>();
                                petList.clear();

                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String petName = documentSnapshot.getString("name");
                                    Pet pet = documentSnapshot.toObject(Pet.class);
                                    petNameList.add(petName);
                                    petList.add(pet);
                                }

                                pets = petNameList.toArray(new String[0]);

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ConsultationActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        pets);
                                etPetName.setAdapter(adapter);
                            }
                        });
            }
        });
        etPetName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPetId[0] = petList.get(position).getPetId();
                etPetType.setText(petList.get(position).getType());
                etPetBreed.setText(petList.get(position).getBreed());
            }
        });

        final Date[] _selectedDateTime = new Date[1];
        _selectedDateTime[0] = consultation.getConsultationDate();
        etConsultationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ConsultationActivity.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(ConsultationActivity.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        _selectedDateTime[0] = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        etConsultationDate.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultationActivity.this);
        builder.setTitle(R.string.edit_alert)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the item in Firestore
                if (TextUtils.isEmpty(etUserName.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati nume utilizator!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetName.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati nume animal companie!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetType.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati tip animal!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPetBreed.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti rasa animalului de companie!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etSubject.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti subiect consultatie!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etRecommendation.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Introduceti recomandare!", Toast.LENGTH_SHORT).show();
                }  else if (TextUtils.isEmpty(etConsultationDate.getText().toString())) {
                    Toast.makeText(ConsultationActivity.this, "Selectati data consultatiei!", Toast.LENGTH_SHORT).show();
                } else {
                    Consultation _consultation = new Consultation(etUserName.getText().toString(), selectedUserID[0], etPetName.getText().toString(),
                            etPetType.getText().toString(), etPetBreed.getText().toString(), selectedPetId[0],
                            etSubject.getText().toString(), etRecommendation.getText().toString(), _selectedDateTime[0]);
                    _consultation.setConsultationId(consultation.getPetId());
                    updateConsultationsInFirestore(_consultation);
                    dialog.dismiss();
                }

            }
        });
    }

    private void updateConsultationsInFirestore(Consultation consultation) {
        Map<String, Object> consultationData = new HashMap<>();
        consultationData.put("consultationDate", consultation.getConsultationDate());
        consultationData.put("consultationId", consultation.getConsultationId());
        consultationData.put("petBreed", consultation.getPetBreed());
        consultationData.put("petId", consultation.getPetId());
        consultationData.put("petName", consultation.getPetName());
        consultationData.put("petType", consultation.getPetType());
        consultationData.put("recommendation", consultation.getRecommendation());
        consultationData.put("subject", consultation.getSubject());
        consultationData.put("userId", consultation.getUserId());
        consultationData.put("userName", consultation.getUserName());

        dbPets.collection("Consultations")
                .document(consultation.getPetId())
                .update(consultationData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Modificare efecutata");
                        // folosim fetch din nou pentru a viuzualiza ultimele modificari
                        fetchConsultationFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare modificare", e);
                    }
                });
    }
}