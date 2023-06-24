package com.example.petcareapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.model.Alerts;
import com.example.petcareapp.model.Consultation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsultationsAdapter extends RecyclerView.Adapter<ConsultationsAdapter.ItemViewHolder> {

    private OnItemLongClickListener onItemLongClickListener;
    private List<Consultation> consultationList;
    private Context mContext;

    public ConsultationsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void setItems(List<Consultation> consultationList) {
        this.consultationList = consultationList;
        notifyDataSetChanged();
    }

    public Consultation getConsultation(int position) {
        if (consultationList != null && position >= 0 && position < consultationList.size()) {
            return consultationList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.list_item_consultation, parent, false);
        return new ConsultationsAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Consultation consultation = consultationList.get(position);
        holder.tvPetName.setText(consultation.getPetName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String selectedDateTime = sdf.format(consultation.getConsultationDate());
        holder.tvDateTime.setText(selectedDateTime);
        holder.tvUserName.setText(consultation.getUserName());
        holder.tvSubject.setText(consultation.getSubject());
        holder.tvPetType.setText(consultation.getPetType());
        holder.tvPetBreed.setText(consultation.getPetBreed());
        holder.tvRecommendation.setText(consultation.getRecommendation());
    }

    @Override
    public int getItemCount() {
        return consultationList != null ? consultationList.size() : 0;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubject, tvDateTime, tvUserName, tvPetType, tvPetName, tvPetBreed, tvRecommendation;
        private ImageButton btnMoreDetails;
        private LinearLayout llMoreDetails;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            btnMoreDetails = itemView.findViewById(R.id.btnMoreDetails);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            llMoreDetails = itemView.findViewById(R.id.llMoreDetails);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPetType = itemView.findViewById(R.id.tvPetType);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvPetBreed = itemView.findViewById(R.id.tvPetBreed);
            tvRecommendation = itemView.findViewById(R.id.tvRecommendation);

            btnMoreDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (llMoreDetails.getVisibility() == View.VISIBLE){
                        btnMoreDetails.setRotation(0);
                        llMoreDetails.setVisibility(View.GONE);
                    }else {
                        btnMoreDetails.setRotation(180);
                        llMoreDetails.setVisibility(View.VISIBLE);
                    }
                }
            });

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

