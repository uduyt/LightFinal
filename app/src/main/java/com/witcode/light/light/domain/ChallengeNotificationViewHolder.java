package com.witcode.light.light.domain;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;

public class ChallengeNotificationViewHolder extends NotificationViewHolder {
    private TextView tvTitle, tvSubTitle;
    private ImageView ivImage;
    private FloatingActionButton fabAction;
    private View mItemView;
    private Context mContext;



    public ChallengeNotificationViewHolder(View itemView, Context context) {
        super(itemView);

        mContext=context;

        ivImage = (ImageView) itemView.findViewById(R.id.iv_challenge_photo);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_challenge_title);
        tvSubTitle = (TextView) itemView.findViewById(R.id.tv_challenge_subtitle);
        fabAction = (FloatingActionButton) itemView.findViewById(R.id.fab_confirm_challenge);
    }

    @Override
    public void bindData(final int pos, final Notification notification) {
        tvTitle.setText(notification.getTitle());
        tvSubTitle.setText(notification.getSubtitle());

        Picasso.with(mContext).load(notification.getImage()).into(ivImage);

        switch (notification.getAction()) {
            case Notification.MARKET_ACTION:
                fabAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;

            case Notification.RANKING_ACTION:
                fabAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;

            case Notification.START_ACTIVITY_ACTION:
                fabAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;


            default:
                fabAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;
        }

    }
}