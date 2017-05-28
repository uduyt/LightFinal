package backend;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetLights extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;

    public GetLights(Context context,OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;

        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_lights.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
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


