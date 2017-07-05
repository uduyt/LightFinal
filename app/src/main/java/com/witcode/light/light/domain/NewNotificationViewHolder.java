package com.witcode.light.light.domain;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.fragments.HomeFragment;

public class NewNotificationViewHolder extends NotificationViewHolder {
    private TextView tvTitle, tvSubTitle;
    private ImageView ivImage;
    private FrameLayout flClose;
    private View mItemView;
    private Context mContext;
    private HomeFragment mFragment;
    private Button bt1, bt2;



    public NewNotificationViewHolder(View itemView, Context context, HomeFragment fragment) {
        super(itemView);

        mItemView=itemView;
        mContext=context;
        mFragment=fragment;
        ivImage = (ImageView) itemView.findViewById(R.id.iv_challenge_photo);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_challenge_title);
        tvSubTitle = (TextView) itemView.findViewById(R.id.tv_challenge_subtitle);
        flClose = (FrameLayout) itemView.findViewById(R.id.fl_close);
        bt1 = (Button) itemView.findViewById(R.id.bt_new1);
        bt2 = (Button) itemView.findViewById(R.id.bt_new2);
    }

    @Override
    public void bindData(final int pos, final Notification notification) {
        tvTitle.setText(notification.getTitle());
        tvSubTitle.setText(notification.getSubtitle());

        Picasso.with(mContext).load(notification.getImage()).into(ivImage);

        bt1.setText(notification.getButton1Text());
        bt2.setText(notification.getButton2Text());

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

        if(notification.getButton1Action()== Notification.NONE){
            bt1.setVisibility(View.GONE);


        }else{
            bt1.setVisibility(View.VISIBLE);

            switch (notification.getButton1Action()) {
                case Notification.MARKET_ACTION:
                    bt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("main");
                        }
                    });
                    break;

                case Notification.RANKING_ACTION:
                    bt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("ranking");
                        }
                    });
                    break;

                case Notification.START_ACTIVITY_ACTION:
                    bt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("start_activity");
                        }
                    });
                    break;


                default:

                    break;
            }
        }

        if(notification.getButton2Action()== Notification.NONE){
            bt2.setVisibility(View.GONE);
        }else{
            bt2.setVisibility(View.VISIBLE);

            switch (notification.getButton2Action()) {
                case Notification.MARKET_ACTION:
                    bt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("main");
                        }
                    });
                    break;

                case Notification.RANKING_ACTION:
                    bt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("ranking");
                        }
                    });
                    break;

                case Notification.START_ACTIVITY_ACTION:
                    bt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).GoToFragment("start_activity");
                        }
                    });
                    break;

                default:

                    break;
            }
        }



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