package com.witcode.light.light.domain;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.witcode.light.light.R;
import com.witcode.light.light.fragments.HomeFragment;

import java.util.List;

public class NotificationsAdapter
        extends RecyclerView.Adapter<NotificationViewHolder>

{

    private List<Notification> mNotifications;
    private Context mContext;
    private HomeFragment mFragment;

    public NotificationsAdapter(List<Notification> notifications, HomeFragment fragment,Context context) {

        mNotifications = notifications;
        mContext = context;
        mFragment=fragment;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int pos) {

        View view;
        NotificationViewHolder vh;


        switch (mNotifications.get(pos).getType()) {
            case Notification.CHALLENGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext, mFragment);
                Log.v("tagg","challenge");
                break;
            case Notification.MARKET:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_market, parent, false);
                vh=new MarketNotificationViewHolder(view, mContext, mFragment);
                Log.v("tagg","market");
                break;
            case Notification.NEW:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_new, parent, false);
                vh=new NewNotificationViewHolder(view, mContext, mFragment);
                Log.v("tagg","new");
                break;
            case Notification.TWEET:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext, mFragment);
                break;
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notif_challenge, parent, false);
                vh=new ChallengeNotificationViewHolder(view, mContext, mFragment);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int pos) {

        holder.bindData(pos, mNotifications.get(pos));
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return position;
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }
}