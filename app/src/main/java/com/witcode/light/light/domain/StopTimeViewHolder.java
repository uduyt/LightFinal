package com.witcode.light.light.domain;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;

public class StopTimeViewHolder extends RecyclerView.ViewHolder {
    private TextView tvLine, tvDestination, tvTimelapse;
    private View mItemView;
    private Context mContext;

    public StopTimeViewHolder(View itemView, Context context) {
        super(itemView);
        mItemView = itemView;
        mContext = context;
        tvLine = (TextView) itemView.findViewById(R.id.tv_line);
        tvDestination = (TextView) itemView.findViewById(R.id.tv_destination);
        tvTimelapse = (TextView) itemView.findViewById(R.id.tv_timelapse);
    }

    public void bindData(final int pos, final Bundle stopTime) {
        tvLine.setText(stopTime.getString("line"));
        tvDestination.setText(stopTime.getString("destination"));
        tvTimelapse.setText(stopTime.getString("timelapse") + "min");
    }
}