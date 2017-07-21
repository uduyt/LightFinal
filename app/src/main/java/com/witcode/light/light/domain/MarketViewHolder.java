package com.witcode.light.light.domain;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.R;

public class MarketViewHolder extends RecyclerView.ViewHolder {
    private TextView tvName, tvLights, tvDiscount;
    private ImageView ivPhoto, ivDiscount;
    private View mItemView;
    private Context mContext;

    public MarketViewHolder(View itemView, Context context) {
        super(itemView);
        mItemView = itemView;
        mContext = context;
        tvName = (TextView) itemView.findViewById(R.id.tv_market_name);
        tvLights = (TextView) itemView.findViewById(R.id.tv_lights);
        tvDiscount = (TextView) itemView.findViewById(R.id.tv_discount);
        ivPhoto = (ImageView) itemView.findViewById(R.id.iv_market_photo);
        ivDiscount = (ImageView) itemView.findViewById(R.id.iv_market_discount);
    }

    public void bindData(final int pos, final MarketItem market) {
        tvName.setText(market.getName());
        tvLights.setText(market.getLights());
        tvDiscount.setText(market.getDiscount());
        Picasso.with(mContext).load("http://sustainabilight.com/fotos/market/market_photo_" + market.getId() + ".jpg").into(ivPhoto);
        Picasso.with(mContext).load("http://sustainabilight.com/fotos/market/market_discount.png").into(ivDiscount);
        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)mContext).GoToMarketDetail(market);
            }
        });
    }
}