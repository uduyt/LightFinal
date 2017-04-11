package com.witcode.light.light;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RankingViewHolder extends RecyclerView.ViewHolder {
    private TextView tvPos, tvName,tvLights;
    private View mItemView;
    private Context mContext;
    private Bundle mRanking;
    private View vBackground;

    public RankingViewHolder(View itemView, Context context) {
        super(itemView);
        mItemView = itemView;
        mContext = context;
        tvPos = (TextView) itemView.findViewById(R.id.tv_ranking_place);
        tvName = (TextView) itemView.findViewById(R.id.tv_ranking_name);
        tvLights = (TextView) itemView.findViewById(R.id.tv_ranking_lights);
        vBackground = itemView.findViewById(R.id.v_ranking_back);
    }

    public void bindData(final int pos, final Bundle ranking) {
        tvPos.setText(String.valueOf(pos+1));
        tvName.setText(ranking.getString("name"));
        tvLights.setText(ranking.getString("lights"));
        if(Profile.getCurrentProfile().getId().equals(ranking.getString("uid"))){
            vBackground.setVisibility(View.VISIBLE);
        }else{
            vBackground.setVisibility(View.GONE);
        }

        switch (pos+1){
            case 1:
                tvPos.setBackground(mContext.getResources().getDrawable(R.drawable.back_ranking_place_gold));
                break;
            case 2:
                tvPos.setBackground(mContext.getResources().getDrawable(R.drawable.back_ranking_place_silver));
                break;
            case 3:
                tvPos.setBackground(mContext.getResources().getDrawable(R.drawable.back_ranking_place_bronze));
                break;
            default:
                tvPos.setBackground(mContext.getResources().getDrawable(R.drawable.back_ranking_place_primary));
                break;

        }
    }
}