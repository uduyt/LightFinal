 package com.witcode.light.light.domain;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.witcode.light.light.R;

import java.util.ArrayList;
import java.util.List;

 public class StopTimeAdapter
         extends RecyclerView.Adapter<StopTimeViewHolder>

 {

     private ArrayList<Bundle> mStopTimes;
     private Context mContext;

     public StopTimeAdapter(ArrayList<Bundle> stopTimes, Context context) {

         mStopTimes=stopTimes;
         mContext = context;
     }

     @Override
     public StopTimeViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
         View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.item_stop_time, parent, false);

         return new StopTimeViewHolder(view, mContext);
     }

     @Override
     public void onBindViewHolder(StopTimeViewHolder holder, int pos) {
         holder.bindData(pos, mStopTimes.get(pos));
     }

     @Override
     public int getItemCount() {
         return mStopTimes.size();
     }
 }