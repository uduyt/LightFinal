package backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ValidateBus extends AsyncTask<Object, String, String> {

    private Context mContext;
    private JSONArray data;
    private OnTaskCompletedListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG = "tagg";
    public static final int VALIDATED = 1;
    public static final int TOO_FAR = 2;
    public static final int JSON_EXCEPTION = 3;
    private Location mLocation;
    private String mLine;

    public ValidateBus(String line, Location location, OnTaskCompletedListener listener) {
        mCallback = listener;
        mLocation = location;
        mLine = line;
    }

    @Override
    protected String doInBackground(Object... params) {

        BufferedReader reader = null;
        String responseString = null;

        try {
            final String REQUEST_BASE_URL =
                    "http://www.sustainabilight.com/functions/validate_bus.php?";

            Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                    .appendQueryParameter("latitude", String.valueOf(mLocation.getLatitude()))
                    .appendQueryParameter("longitude", String.valueOf(mLocation.getLongitude()))
                    .build();

            URL url = new URL(builtUri.toString());


            Log.v("mytag", "Built URI " + builtUri.toString());
            // Open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                publishProgress("no input stream");
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            responseString = reader.readLine();

        } catch (IOException e) {

            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    publishProgress("try to close reader");
                    return null;
                }
            }

        }
        return responseString;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        Toast.makeText(mContext, values[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String result) {

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
                        mCallback.OnComplete("", ValidateBus.VALIDATED);
                        return;
                    }
                } else {
                    //array
                    jsonLines = jsonStop.getJSONArray("line");
                    for (int j = 0; j < jsonLines.length(); j++) {
                        if (jsonLines.getJSONObject(j).getString("line").equals(mLine)) {
                            mCallback.OnComplete("", ValidateBus.VALIDATED);
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
                            mCallback.OnComplete("", ValidateBus.VALIDATED);
                            return;
                        }
                    } else {
                        //array
                        jsonLines = jsonStop.getJSONArray("line");
                        for (int j = 0; j < jsonLines.length(); j++) {
                            if (jsonLines.getJSONObject(j).getString("line").equals(mLine)) {
                                mCallback.OnComplete("", ValidateBus.VALIDATED);
                                return;
                            }
                        }
                    }
                }
            }


            mCallback.OnComplete(result, ValidateBus.TOO_FAR);
        } catch (JSONException e) {
            e.printStackTrace();
            mCallback.OnComplete(result, ValidateBus.TOO_FAR);
        }
    }
}


