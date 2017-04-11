package backend;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.lang.StringEscapeUtils;
import com.facebook.Profile;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetRankings extends AsyncTask<Object, String, String> {

    private Context mContext;
    private JSONArray data;
    private OnTaskCompletedListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG="tagg";
    public static final int SUCCESSFUL=1;

    public GetRankings(OnTaskCompletedListener listener) {
        mCallback=listener;
    }

    @Override
    protected String doInBackground(Object... params) {

        BufferedReader reader=null;
        String responseString = null;

        try {
            final String REQUEST_BASE_URL =
                    "http://www.sustainabilight.com/functions/get_rankings.php?";

            Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                    .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
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
        result=StringEscapeUtils.unescapeJava(result);
        if (result!=null) {
            mCallback.OnComplete(result,SUCCESSFUL);
        }
    }
}


