package backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

import org.json.JSONArray;

import java.net.HttpURLConnection;

public class ExceptionHandler extends MyServerClass implements OnTaskCompletedListener {

    private String mResult;
    private int mResultCode, mResultType;
    public static final int SUCCESSFUL=1;

    public ExceptionHandler(String result) {
        mResult=result;

        SetUp();
    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/handle_exception.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("exception", mResult)
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {


    }

}


