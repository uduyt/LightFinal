package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.witcode.light.light.R;

public class LoginTask extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private Context mContext;
    private String mGender;
    private String mEmail;
    public static final int SUCCESSFUL=1;
    public static final int NEED_UPDATE=2;

    public LoginTask(Context context, String gender, String email, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mGender=gender;
        mEmail=email;
        mContext=context;

        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/login.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("firebase_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .appendQueryParameter("name", Profile.getCurrentProfile().getName())
                .appendQueryParameter("second_name", Profile.getCurrentProfile().getLastName())
                .appendQueryParameter("gender",mGender)
                .appendQueryParameter("email", mEmail)
                .appendQueryParameter("app_version", mContext.getString(R.string.str_version_number))
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
            if (result.equals("ok")) {
                mCallback.OnComplete(result,LoginTask.SUCCESSFUL,MyServerClass.SUCCESSFUL);
            }else if(result.equals("update")){
                mCallback.OnComplete(result,LoginTask.NEED_UPDATE,MyServerClass.SUCCESSFUL);
            }
        }

    }

}

