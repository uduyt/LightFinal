package backend;

import android.net.Uri;

import com.facebook.Profile;

public class UpdateLights extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private int lights;

    public UpdateLights(int l, OnTaskCompletedListener listener) {
        mCallback=listener;
        lights=l;

        SetUp();
    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/update_lights.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("lights",String.valueOf(lights))
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if(resultType!=MyServerClass.SUCCESSFUL){
            if(resultCode==MyServerClass.NULL_RESULT){
                //maybe it doesnt matter
            }

            //Send exception to server


            //Send listener back
            mCallback.OnComplete(result,resultCode,resultType);

        }else{
            if (result.equals("ok")) {
                mCallback.OnComplete(result,SUCCESSFUL, SUCCESSFUL);
            }
        }

    }

}




