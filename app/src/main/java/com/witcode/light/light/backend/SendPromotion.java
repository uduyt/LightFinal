package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;
import com.witcode.light.light.domain.MarketItem;

public class SendPromotion extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private MarketItem mMarket;

    public SendPromotion(Context context, MarketItem marketItem, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mMarket=marketItem;

        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/send_promotion.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("user_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("user_name", Profile.getCurrentProfile().getName())
                .appendQueryParameter("market_id", mMarket.getId())
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if (resultType != MyServerClass.SUCCESSFUL) {
            if (resultCode == MyServerClass.NULL_RESULT) {
                //maybe it doesnt matter
            }

            //Send exception to server


            //Send listener back
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            mCallback.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }

}



