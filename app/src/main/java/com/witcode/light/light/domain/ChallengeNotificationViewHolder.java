package com.witcode.light.light.domain;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.fragments.HomeFragment;

public class ChallengeNotificationViewHolder extends NotificationViewHolder {
    private TextView tvTitle, tvSubTitle;
    private ImageView ivImage, ivFav;
    private FrameLayout flClose;
    private View mItemView;
    private Context mContext;
    private HomeFragment mFragment;



    public ChallengeNotificationViewHolder(View itemView, Context context, HomeFragment fragment) {
        super(itemView);

        mItemView=itemView;
        mContext=context;
        mFragment=fragment;
        ivImage = (ImageView) itemView.findViewById(R.id.iv_challenge_photo);
        ivFav = (ImageView) itemView.findViewById(R.id.iv_fav);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_challenge_title);
        tvSubTitle = (TextView) itemView.findViewById(R.id.tv_challenge_subtitle);
        tvSubTitle = (TextView) itemView.findViewById(R.id.tv_challenge_subtitle);
        flClose = (FrameLayout) itemView.findViewById(R.id.fl_close);
    }

    @Override
    public void bindData(final int pos, final Notification notification) {
        tvTitle.setText(notification.getTitle());
        tvSubTitle.setText(notification.getSubtitle());

        Picasso.with(mContext).load(notification.getImage()).into(ivImage);

        if(notification.isCloseable()){
            flClose.setVisibility(View.VISIBLE);
        }else{
            flClose.setVisibility(View.GONE);
        }

        flClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.RemoveNotification(pos,notification.getId());
            }
        });

        ivFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fav= notification.isFav() ? "no" : "yes";
                mFragment.ToggleFav(notification.getId(),fav);
                if(notification.isFav()){
                    ivFav.setImageResource(R.drawable.ic_star_border);
                    notification.setFav(false);
                }else{
                    ivFav.setImageResource(R.drawable.ic_star);
                    notification.setFav(true);
                }
            }
        });

        if(notification.isFav()){
            ivFav.setImageResource(R.drawable.ic_star);
        }else{
            ivFav.setImageResource(R.drawable.ic_star_border);
        }

        if(notification.getTitle().equals("none")){
            tvTitle.setVisibility(View.GONE);
        }else{
            tvTitle.setVisibility(View.VISIBLE);
        }

        if(notification.getSubtitle().equals("none")){
            tvSubTitle.setVisibility(View.GONE);
        }else{
            tvSubTitle.setVisibility(View.VISIBLE);
        }

        switch (notification.getAction()) {
            case Notification.MARKET_ACTION:
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;

            case Notification.RANKING_ACTION:
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;

            case Notification.START_ACTIVITY_ACTION:
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).GoToFragment("main");
                    }
                });
                break;


            default:

                break;
        }

    }
}