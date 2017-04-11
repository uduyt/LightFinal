 package com.witcode.light.light;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter
        extends RecyclerView.Adapter<RankingViewHolder>

{

    private List<Bundle> mRankings;
    private Context mContext;

    public RankingAdapter(List<Bundle> rankings, Context context) {

        mRankings=rankings;
        mContext = context;
    }

    @Override
    public RankingViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);

        return new RankingViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(RankingViewHolder holder, int pos) {

        holder.bindData(pos, mRankings.get(pos));
    }

    @Override
    public int getItemCount() {
        return mRankings.size();
    }
}