 package com.witcode.light.light;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

 public class MarketItemAdapter
         extends RecyclerView.Adapter<MarketViewHolder>

 {

     private List<MarketItem> mMarkets;
     private Context mContext;

     public MarketItemAdapter(List<MarketItem> markets, Context context) {

         mMarkets=markets;
         mContext = context;
     }

     @Override
     public MarketViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
         View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.item_market, parent, false);

         return new MarketViewHolder(view, mContext);
     }

     @Override
     public void onBindViewHolder(MarketViewHolder holder, int pos) {

         holder.bindData(pos, mMarkets.get(pos));
     }

     @Override
     public int getItemCount() {
         return mMarkets.size();
     }
 }