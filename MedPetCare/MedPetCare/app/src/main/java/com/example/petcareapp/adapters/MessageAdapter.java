package com.example.petcareapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.petcareapp.R;
import com.example.petcareapp.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {
    private List<Message> messages;
    private LayoutInflater inflater;

    public MessageAdapter(Context context) {
        messages = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public List<Message> getMessages() {
        return messages;
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.item_message, parent, false);
            holder = new ViewHolder();
            holder.senderMessageTextView = view.findViewById(R.id.senderMessageTextView);
            holder.senderTimeTextView = view.findViewById(R.id.senderTimeTextView);
            holder.receiverMessageTextView = view.findViewById(R.id.receiverMessageTextView);
            holder.receiverTimeTextView = view.findViewById(R.id.receiverTimeTextView);
            holder.rlReceiver = view.findViewById(R.id.rlReceiver);
            holder.rlSender = view.findViewById(R.id.rlSender); // Add time TextView
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Message message = getItem(position);

        if (message.isCurrentUser()) {
            holder.rlReceiver.setVisibility(View.GONE);
            holder.rlSender.setVisibility(View.VISIBLE);
            holder.senderMessageTextView.setText(message.getMessage());
            if (message.getTime() != null) {
                holder.senderTimeTextView.setText(formatTime(message.getTime()));// Set the formatted time
            }
        }else {
            holder.rlReceiver.setVisibility(View.VISIBLE);
            holder.rlSender.setVisibility(View.GONE);
            holder.receiverMessageTextView.setText(message.getMessage());
            if (message.getTime() != null) {
                holder.receiverTimeTextView.setText(formatTime(message.getTime()));// Set the formatted time
            }
        }

        return view;
    }

    private String formatTime(Date time) {
        // Format the time as per your requirements
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(time);
    }

    private static class ViewHolder {
        TextView senderMessageTextView, senderTimeTextView;
        TextView receiverMessageTextView, receiverTimeTextView;
        LinearLayout rlReceiver, rlSender;
    }
}


