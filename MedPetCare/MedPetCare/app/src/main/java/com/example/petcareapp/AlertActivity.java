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

import com.example.petcareapp.adapters.AlertsAdapter;
import com.example.petcareapp.adapters.PetsAdapter;
import com.example.petcareapp.model.Alerts;
import com.example.petcareapp.model.Pet;
import com.example.petcareapp.utils.Constants;
import com.example.petcareapp.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlertActivity extends AppCompatActivity implements AlertsAdapter.OnItemLongClickListener {

    private ImageButton btnBack;
    private FirebaseFirestore dbPets;
    private AlertsAdapter alertsAdapter;
    private PreferenceUtils preferenceUtils;
    private RecyclerView rvAlerts;
    private TextView empty_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        preferenceUtils = new PreferenceUtils(AlertActivity.this);

        // Initialize Firebase Firestore
        dbPets = FirebaseFirestore.getInstance();

        rvAlerts = findViewById(R.id.rvAlerts);
        empty_view = findViewById(R.id.empty_view);
        alertsAdapter = new AlertsAdapter(AlertActivity.this);
        alertsAdapter.setOnItemLongClickListener(this);
        rvAlerts.setHasFixedSize(true);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvAlerts.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        rvAlerts.addItemDecoration(divider);
        rvAlerts.setAdapter(alertsAdapter);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        FloatingActionButton fab = findViewById(R.id.addAlerts);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlerts();
            }
        });

        // Fetch items from Firestore and update RecyclerView
        fetchAlertsFromFirestore();
    }

    private void fetchAlertsFromFirestore() {
        dbPets.collection("alerts")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("myAlerts")
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Alerts> itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Alerts alerts = document.toObject(Alerts.class);
                                itemList.add(alerts);
                            }
                            alertsAdapter.setItems(itemList);
                            if (itemList.isEmpty()) {
                                empty_view.setVisibility(View.VISIBLE);
                            } else {
                                empty_view.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Firestore", "Eroare baza de date: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onItemLongClick(int position) {
        Alerts alerts = alertsAdapter.getAlerts(position);
        showOptionsDialog(alerts);
    }

    private void addAlerts() {
        final String[] alertsTypes = {"Vizita veterinar", "Vizita salon", "Plimbare", "Medicatie", "Achizite hrana", "Vaccin", "Sterilizare"};

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_alerts, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AlertActivity.this);
        builder.setCancelable(false);

        TextInputEditText etDateTime = view.findViewById(R.id.etDateTime);
        MaterialAutoCompleteTextView etAlertType = view.findViewById(R.id.etAlertType);

        etAlertType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etAlertType.showDropDown();
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                alertsTypes);
        etAlertType.setAdapter(adapter);

        etAlertType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) parent.getItemAtPosition(position);
                Toast.makeText(AlertActivity.this, "Alerta selectata: " + selectedType, Toast.LENGTH_SHORT).show();
            }
        });

        final Date[] _selectedDateTime = new Date[1];
        etDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AlertActivity.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(AlertActivity.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        _selectedDateTime[0] = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        etDateTime.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        builder.setTitle(R.string.add_alerts)
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
                TextInputEditText etPetName = view.findViewById(R.id.etPetName);
                TextInputEditText etDateTime = view.findViewById(R.id.etDateTime);
                MaterialAutoCompleteTextView etAlertType = view.findViewById(R.id.etAlertType);

                Log.e("selectedDateTimeInMillis", _selectedDateTime[0].toString() + " **");
                if (TextUtils.isEmpty(etPetName.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Selecteaza nume animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(etDateTime.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Selecteaza data si ora!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(etAlertType.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Seleteaza tipul alertei!", Toast.LENGTH_SHORT).show();
                }else {
                    addAlertsToFirestore(etPetName.getText().toString(), _selectedDateTime[0], etAlertType.getText().toString());
                    dialog.dismiss();
                }
            }
        });
    }

    private void addAlertsToFirestore(String petName, Date dateTime, String alertType) {
        Alerts alerts = new Alerts(petName, alertType, dateTime);

        dbPets.collection("alerts")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("myAlerts")
                .add(alerts)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String itemId = documentReference.getId();  // Retrieve the item ID
                        Log.d("Firestore", "Item added with ID: " + itemId);

                        alerts.setAlertId(itemId);

                        documentReference.set(alerts)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "Item data updated with item ID: " + itemId);

                                        fetchAlertsFromFirestore();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Error updating item data with item ID", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error adding item", e);
                    }
                });
    }
    private void showOptionsDialog(final Alerts alerts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Optiuni");

        CharSequence[] options = {"Editeaza", "Sterge"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showEditDialog(alerts);
                } else if (which == 1) {
                    deleteItem(alerts);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem(Alerts alerts) {
        dbPets.collection("alerts")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("myAlerts")
                .document(alerts.getAlertId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Alerta stearsa");
                        fetchAlertsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare stergere alerta", e);
                    }
                });
    }

    private void showEditDialog(final Alerts alerts) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_alerts, null);
        final TextInputEditText etPetName = view.findViewById(R.id.etPetName);
        etPetName.setText(alerts.getPetName());
        final TextInputEditText etDateTime = view.findViewById(R.id.etDateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String selectedDateTime = sdf.format(alerts.getDateTime());
        etDateTime.setText(selectedDateTime);
        final AutoCompleteTextView etAlertType = view.findViewById(R.id.etAlertType);
        etAlertType.setText(alerts.getAlertType());

        final Date[] _selectedDateTime = new Date[1];
        _selectedDateTime[0] = alerts.getDateTime();
        etDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AlertActivity.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(AlertActivity.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        _selectedDateTime[0] = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        etDateTime.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(AlertActivity.this);
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
                // actualizare item in baza de date
                if (TextUtils.isEmpty(etPetName.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Introduceti nume animal companie!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(etAlertType.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Introduceti tiopul alertei!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(etDateTime.getText().toString())) {
                    Toast.makeText(AlertActivity.this, "Selectati data!", Toast.LENGTH_SHORT).show();
                }else {
                    updateItemInFirestore(alerts.getAlertId(), etPetName.getText().toString(), etAlertType.getText().toString(), _selectedDateTime[0]);
                    dialog.dismiss();
                }
            }
        });
    }

    private void updateItemInFirestore(String alertId, String petName, String alertType, Date date) {
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("alertId", alertId);
        alertData.put("alertType", alertType);
        alertData.put("dateTime", date);
        alertData.put("petName", petName);

        dbPets.collection("alerts")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("myAlerts")
                .document(alertId)
                .update(alertData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Item updated");
                        // Fetch items again to reflect the changes
                        fetchAlertsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error updating item", e);
                    }
                });
    }
}