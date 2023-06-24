package com.example.petcareapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.petcareapp.R;
import com.example.petcareapp.model.Message;
import com.example.petcareapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    private List<User> users;
    private LayoutInflater inflater;

    public UserAdapter(Context context) {
        users = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public void setUsers(List<User> userList) {
        users.clear();
        users.addAll(userList);
        notifyDataSetChanged();
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
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
            view = inflater.inflate(R.layout.item_user, parent, false);
            holder = new ViewHolder();
            holder.usernameTextView = view.findViewById(R.id.usernameTextView);
            holder.userEmailTextView = view.findViewById(R.id.userEmailTextView);
            holder.userTypeTextView = view.findViewById(R.id.userTypeTextView);
            holder.tvUnreadCount = view.findViewById(R.id.tvUnreadCount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        User user = getItem(position);

        holder.usernameTextView.setText(user.getPrenume() + " " + user.getNume());
        holder.userEmailTextView.setText(user.getEmail());
        holder.userTypeTextView.setText(user.getUserType());
        int unreadCount = calculateUnreadCount(user.getUserId());
        if (unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        return view;
    }

    private int calculateUnreadCount(String userId) {
        int unreadCount = 0;
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                unreadCount += user.getUnreadCount();
            }
        }
        return unreadCount;
    }

    private static class ViewHolder {
        TextView usernameTextView, userEmailTextView, userTypeTextView, tvUnreadCount;
    }
}

