package com.example.petcareapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.model.Alerts;
import com.example.petcareapp.model.Pet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ItemViewHolder> {

    private OnItemLongClickListener onItemLongClickListener;
    private List<Alerts> alertsList;
    private Context mContext;

    public AlertsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void setItems(List<Alerts> alertsList) {
        this.alertsList = alertsList;
        notifyDataSetChanged();
    }

    public Alerts getAlerts(int position) {
        if (alertsList != null && position >= 0 && position < alertsList.size()) {
            return alertsList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.list_item_alert, parent, false);
        return new AlertsAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Alerts alerts = alertsList.get(position);
        holder.tvPetName.setText(alerts.getPetName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String selectedDateTime = sdf.format(alerts.getDateTime());
        holder.tvDateTime.setText(selectedDateTime);
        holder.tvAlertType.setText(alerts.getAlertType());
        // Compare selected date with current date
        Date currentDate = new Date();
        Date selectedDate = alerts.getDateTime();

// Compare dates using the compareTo method
        int comparisonResult = selectedDate.compareTo(currentDate);

        if (comparisonResult < 0) {
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
        }
        switch (alerts.getAlertType()){
            case "Vizita veterinar":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_veterinary));
                break;
            case "Vizita salon":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_visit_salon));
                break;
            case "Plimbare":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_walk));
                break;
            case "Medicatie":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_medication));
                break;
            case "Achizite hrana":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_food));
                break;
            case "Vaccin":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_vaccine));
                break;
            case "Sterilizare":
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sterilization));
                break;
            default:
                holder.ivAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_medication));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return alertsList != null ? alertsList.size() : 0;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPetName, tvDateTime, tvAlertType;
        private ImageView ivAlert;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            ivAlert = itemView.findViewById(R.id.ivAlert);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvAlertType = itemView.findViewById(R.id.tvAlertType);

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

