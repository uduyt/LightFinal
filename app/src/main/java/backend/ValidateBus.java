package backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class ValidateBus extends MyServerClass implements OnTaskCompletedListener {


    private OnTaskCompletedListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG="tagg";
    public static final int VALIDATED = 1;
    public static final int TOO_FAR = 2;
    private Location mLocation;
    private String mLine;

    public ValidateBus(Context context, String line, Location location, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLocation = location;
        mLine = line;

        SetUp();
    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/validate_bus.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("latitude", String.valueOf(mLocation.getLatitude()))
                .appendQueryParameter("longitude", String.valueOf(mLocation.getLongitude()))
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
            //Send listener back
            mCallback.OnComplete(result,resultCode,resultType);

        }else {
            HandleResult(result);
        }

    }

    private void HandleResult(String result){
        try {
            JSONObject mainJSON = new JSONObject(result);

            JSONObject dataObject1 = mainJSON.optJSONObject("stop");

            if (dataObject1 != null) {

                //object.
                JSONArray jsonLines;
                JSONObject jsonStop;

                jsonStop = mainJSON.getJSONObject("stop");

                JSONObject dataObject = jsonStop.optJSONObject("line");

                if (dataObject != null) {
                    //object.
                    if (jsonStop.optJSONObject("line").getString("line").equals(mLine)) {
                        mCallback.OnComplete("", ValidateBus.VALIDATED, SUCCESSFUL);
                        return;
                    }
                } else {
                    //array
                    jsonLines = jsonStop.getJSONArray("line");
                    for (int j = 0; j < jsonLines.length(); j++) {
                        if (jsonLines.getJSONObject(j).getString("line").equals(mLine)) {
                            mCallback.OnComplete("", ValidateBus.VALIDATED, SUCCESSFUL);
                            return;
                        }
                    }
                }
            } else {

                //array
                JSONArray jsonStops = mainJSON.getJSONArray("stop");

                JSONArray jsonLines;
                JSONObject jsonStop;
                JSONObject jsonLine;

                for (int i = 0; i < jsonStops.length(); i++) {
                    jsonStop = jsonStops.getJSONObject(i);

                    JSONObject dataObject = jsonStop.optJSONObject("line");

                    if (dataObject != null) {
                        //object.
                        if (jsonStop.optJSONObject("line").getString("line").equals(mLine)) {
                            mCallback.OnComplete("", ValidateBus.VALIDATED,SUCCESSFUL);
                            return;
                        }
                    } else {
                        //array
                        jsonLines = jsonStop.getJSONArray("line");
                        for (int j = 0; j < jsonLines.length(); j++) {
                            if (jsonLines.getJSONObject(j).getString("line").equals(mLine)) {
                                mCallback.OnComplete("", ValidateBus.VALIDATED, SUCCESSFUL);
                                return;
                            }
                        }
                    }
                }
            }

            mCallback.OnComplete(result, ValidateBus.TOO_FAR, SUCCESSFUL);
        } catch (JSONException e) {
            e.printStackTrace();
            mCallback.OnComplete(result, ValidateBus.TOO_FAR, SUCCESSFUL);
        }
    }

}


