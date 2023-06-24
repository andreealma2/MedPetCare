package com.example.petcareapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.CatalogAdapter;
import com.example.petcareapp.R;
import com.example.petcareapp.model.Pet;

import java.util.List;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.ItemViewHolder> {

    private OnItemLongClickListener onItemLongClickListener;
    private List<Pet> petList;
    private Context mContext;

    public PetsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void setItems(List<Pet> petList) {
        this.petList = petList;
        notifyDataSetChanged();
    }

    public Pet getPet(int position) {
        if (petList != null && position >= 0 && position < petList.size()) {
            return petList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.list_item, parent, false);
        return new PetsAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.nameTextView.setText(pet.getName());
        holder.breedTextView.setText(pet.getBreed());
        holder.tvDob.setText(pet.getDateOfBirth());
        holder.tvSterilized.setText(pet.getSterilized());
        holder.tvType.setText(pet.getType());
        holder.tvWeight.setText(pet.getWeight());
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0;
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView, tvDob, tvWeight, tvSterilized, tvType;
        private TextView breedTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            breedTextView = itemView.findViewById(R.id.breed_text_view);
            tvDob = itemView.findViewById(R.id.tvDob);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvSterilized = itemView.findViewById(R.id.tvSterilized);
            tvType = itemView.findViewById(R.id.tvType);

            itemView.setFocusable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemLongClickListener.onItemLongClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
}

