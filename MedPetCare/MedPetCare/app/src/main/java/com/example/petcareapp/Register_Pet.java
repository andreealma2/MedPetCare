package com.example.petcareapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcareapp.adapters.PetsAdapter;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Register_Pet extends AppCompatActivity implements PetsAdapter.OnItemLongClickListener {

    private ArrayList<Pet> mData;
    private List<Pet> petList = new ArrayList<>();
    private PetsAdapter petsAdapter;
    private FirebaseFirestore dbPets;
    private ArrayList<String> mDataId;
    private CatalogAdapter mAdapter;
    private ImageButton btnBack;
    private PreferenceUtils preferenceUtils;
    private androidx.appcompat.view.ActionMode mActionMode;

    private DatabaseReference mDatabase;
    private TextView empty_view;
    private ChildEventListener childEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            mData.add(dataSnapshot.getValue(Pet.class));
            mDataId.add(dataSnapshot.getKey());
            mAdapter.updateEmptyView();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            int pos = mDataId.indexOf(dataSnapshot.getKey());
            mData.set(pos, dataSnapshot.getValue(Pet.class));
            mAdapter.notifyDataSetChanged();
        }

        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            int pos = mDataId.indexOf(dataSnapshot.getKey());
            mDataId.remove(pos);
            mData.remove(pos);
            mAdapter.updateEmptyView();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pet);
        preferenceUtils = new PreferenceUtils(Register_Pet.this);

        // Initialize Firebase Firestore
        dbPets = FirebaseFirestore.getInstance();

        mData = new ArrayList<>();
        petList = new ArrayList<>();
        mDataId = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("pets");
        mDatabase.addChildEventListener(childEventListener);
        empty_view = findViewById(R.id.empty_view);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_pet);
        petsAdapter = new PetsAdapter(Register_Pet.this);
        petsAdapter.setOnItemLongClickListener(this);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);

        View emptyView = findViewById(R.id.empty_view);
        mAdapter = new CatalogAdapter(this, mData, mDataId, emptyView,
                new CatalogAdapter.ClickHandler() {
                    @Override
                    public void onItemClick(int position) {
                        if (mActionMode != null) {
                            mAdapter.toggleSelection(mDataId.get(position));
                            if (mAdapter.selectionCount() == 0)
                                mActionMode.finish();
                            else
                                mActionMode.invalidate();
                            return;
                        }

                        String pet = mData.get(position).toString();
                        Toast.makeText(Register_Pet.this, pet, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public boolean onItemLongClick(int position) {
                        if (mActionMode != null) return false;

                        mAdapter.toggleSelection(mDataId.get(position));
                        mActionMode = Register_Pet.this.startSupportActionMode((androidx.appcompat.view.ActionMode.Callback) mActionModeCallback);
                        return true;
                    }
                });
        recyclerView.setAdapter(petsAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPet();
            }
        });

        // Actualizare RecylceView cu info din Firestore
        fetchItemsFromFirestore();
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.catalog_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(String.valueOf(mAdapter.selectionCount()));
            menu.findItem(R.id.action_edit).setVisible(mAdapter.selectionCount() == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    editPet();
                    return true;

                case R.id.action_delete:
                    deletePet();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mAdapter.resetSelection();
        }
    };

    private void addPet() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_editor, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(Register_Pet.this);
        builder.setCancelable(false);

        TextInputEditText dob = view.findViewById(R.id.etDob);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        final String[] sterilized = {"Da"};
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    MaterialButton checkedButton = group.findViewById(checkedId);
                    sterilized[0] = checkedButton.getText().toString();
                } else {
                }
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Register_Pet.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(Register_Pet.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        dob.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        builder.setTitle(R.string.add_pet)
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
                TextInputEditText nameTextView = view.findViewById(R.id.name_edit_text);
                TextInputEditText breedTextView = view.findViewById(R.id.breed_edit_text);
                TextInputEditText type = view.findViewById(R.id.etType);
                TextInputEditText weight = view.findViewById(R.id.etWeight);
                TextInputEditText dob = view.findViewById(R.id.etDob);

                if (TextUtils.isEmpty(nameTextView.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti nume animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(breedTextView.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti rasa animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(type.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti tip animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(dob.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti data nasterii!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(weight.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti greutate animal!", Toast.LENGTH_SHORT).show();
                }else {
                    addItemToFirestore(nameTextView.getText().toString(), type.getText().toString(), breedTextView.getText().toString(), dob.getText().toString(), weight.getText().toString(), sterilized[0]);
                    dialog.dismiss();
                }
            }
        });
    }

    private void addItemToFirestore(String name, String type, String breed, String dob, String weight, String sterilized) {
        Pet pet = new Pet(name, type, breed, dob, weight, sterilized);
        pet.setType(type);

        dbPets.collection("registerPets")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("pets")
                .add(pet)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String itemId = documentReference.getId();  // Retrieve the item ID
                        Log.d("Firestore", "Animal companie adaugat: " + itemId);

                        // actualizare petID
                        pet.setPetId(itemId);

                        // actualizare baza de date
                        documentReference.set(pet)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "Animal companie adaugat: " + itemId);

                                        // preluam din nou datele
                                        fetchItemsFromFirestore();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Eroare adaugare animal companie", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare daugare animal", e);
                    }
                });
    }

    private void fetchItemsFromFirestore() {
        dbPets.collection("registerPets")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("pets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Pet> itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Pet pet = document.toObject(Pet.class);
                                itemList.add(pet);
                            }
                            petsAdapter.setItems(itemList);
                            if (itemList.isEmpty()) {
                                empty_view.setVisibility(View.VISIBLE);
                            } else {
                                empty_view.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Firestore", "Eroare accesare informatii: ", task.getException());
                        }
                    }
                });
    }


    private void editPet() {
        final String currentPetId = mAdapter.getSelectedId().get(0);
        Pet selectedPet = mData.get(mDataId.indexOf(currentPetId));

        View view = getLayoutInflater().inflate(R.layout.dialog_editor, null);
        final TextView nameTextView = view.findViewById(R.id.name_edit_text);
        nameTextView.setText(selectedPet.getName());
        final TextView breedTextView = view.findViewById(R.id.breed_edit_text);
        breedTextView.setText(selectedPet.getBreed());

        AlertDialog.Builder builder = new AlertDialog.Builder(Register_Pet.this);
        builder.setTitle(R.string.edit_pet)
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*mDatabase.child(currentPetId).setValue(new Pet(
                                nameTextView.getText().toString(),
                                breedTextView.getText().toString())
                        );*/
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mActionMode.finish();
                    }
                });
        builder.create().show();
    }

    private void deletePet() {
        final ArrayList<String> selectedIds = mAdapter.getSelectedId();
        int message = selectedIds.size() == 1 ? R.string.delete_pet : R.string.delete_pets;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (String currentPetId : selectedIds) {
                            mDatabase.child(currentPetId).removeValue();
                        }
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mActionMode.finish();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onItemLongClick(int position) {
        Pet pet = petsAdapter.getPet(position);
        showOptionsDialog(pet);
    }

    private void showOptionsDialog(final Pet pet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Optiuni");

        CharSequence[] options = {"Editeaza", "Sterge"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showEditDialog(pet);
                } else if (which == 1) {
                    deleteItem(pet);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditDialog(final Pet pet) {
        View view = getLayoutInflater().inflate(R.layout.dialog_editor, null);
        final TextInputEditText nameTextView = view.findViewById(R.id.name_edit_text);
        nameTextView.setText(pet.getName());
        final TextInputEditText breedTextView = view.findViewById(R.id.breed_edit_text);
        breedTextView.setText(pet.getBreed());
        final TextInputEditText type = view.findViewById(R.id.etType);
        type.setText(pet.getType());
        final TextInputEditText dob = view.findViewById(R.id.etDob);
        dob.setText(pet.getDateOfBirth());
        final TextInputEditText weight = view.findViewById(R.id.etWeight);
        weight.setText(pet.getWeight());

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        final String[] sterilized = {"Da"};
        if (pet.getSterilized().equalsIgnoreCase("Da") || TextUtils.isEmpty(pet.getSterilized())){
            int selectedButtonId = R.id.btnYes;
            toggleGroup.check(selectedButtonId);
            sterilized[0] = "Da";
        }else {
            int selectedButtonId = R.id.btnNo;
            toggleGroup.check(selectedButtonId);
            sterilized[0] = "Nu";
        }
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    MaterialButton checkedButton = group.findViewById(checkedId);
                    sterilized[0] = checkedButton.getText().toString();
                    // Use the selectedValue as needed
                } else {
                    // No button is selected
                }
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Register_Pet.this, (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(Register_Pet.this, (timePicker, hourOfDay, minute1) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String selectedDateTime = sdf.format(calendar.getTime());
                        dob.setText(selectedDateTime);
                    }, hour, minute, false);

                    timePickerDialog.show();
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(Register_Pet.this);
        builder.setTitle(R.string.edit_pet)
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
                if (TextUtils.isEmpty(nameTextView.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti nume animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(breedTextView.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti rasa animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(type.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti tip animal!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(dob.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti data nasterii!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(weight.getText().toString())) {
                    Toast.makeText(Register_Pet.this, "Introduceti greutate animal!", Toast.LENGTH_SHORT).show();
                }else {
                    updateItemInFirestore(pet.getPetId(), nameTextView.getText().toString(), breedTextView.getText().toString(), type.getText().toString(), dob.getText().toString(), weight.getText().toString(), sterilized[0]);
                    dialog.dismiss();
                }
            }
        });
    }

    private void deleteItem(Pet pet) {
        dbPets.collection("registerPets")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("pets")
                .document(pet.getPetId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Animal sters din baza de date");
                        // Fetch items again to reflect the changes
                        fetchItemsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare stergere animal", e);
                    }
                });
    }

    private void updateItemInFirestore(String itemId, String name, String breed, String type, String dob, String weight, String sterilized) {
        Map<String, Object> petData = new HashMap<>();
        petData.put("name", name);
        petData.put("breed", breed);
        petData.put("type", type);
        petData.put("dateOfBirth", dob);
        petData.put("weight", weight);
        petData.put("sterilized", sterilized);
        petData.put("petId", itemId);

        dbPets.collection("registerPets")
                .document(preferenceUtils.getString(Constants.PreferenceKeys.USER_ID))
                .collection("pets")
                .document(itemId)
                .update(petData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Informatie actualizata");
                        // Fetch items again to reflect the changes
                        fetchItemsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Eroare modificare date", e);
                    }
                });
    }


}
