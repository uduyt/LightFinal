package com.witcode.light.light.domain;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.witcode.light.light.R;

import java.util.List;

public class NotificationsAdapter
        extends RecyclerView.Adapter<NotificationViewHolder>

{

    private List<Notification> mNotifications;
    private Context mContext;

    public NotificationsAdapter(List<Notification> notifications, Context context) {

        mNotifications = notifications;
        mContext = context;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int pos) {

        View view;
        NotificationViewHolder vh;

        switch (mNotifications.get(pos).getType()) {
            case Notification.CHALLENGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext);
                break;
            case Notification.MARKET:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext);
                break;
            case Notification.NEW:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext);
                break;
            case Notification.TWEET:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext);
                break;
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int pos) {

        holder.bindData(pos, mNotifications.get(pos));
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }
}