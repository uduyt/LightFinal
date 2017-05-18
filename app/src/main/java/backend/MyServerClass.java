package backend;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.witcode.light.light.R;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyServerClass extends AsyncTask<Object, String, String> {

    private HttpURLConnection urlConnection;
    private Uri mUri;
    private OnTaskCompletedListener mListener;
    public static final int NO_INPUT_STREAM_EXCEPTION=1;
    public static final int IOEXCEPTION=2;
    public static final int NULL_RESULT=3;

    public static final int ERROR=1;
    public static final int WARNING=2;
    public static final int SUCCESSFUL=3;

    public MyServerClass() {

    }

    public void setUri(Uri uri){
        mUri=uri;
    }

    public void setListener(OnTaskCompletedListener listener){
        mListener=listener;
    }
    @Override
    protected String doInBackground(Object... params) {

        BufferedReader reader=null;
        String responseString = null;

        try {


            URL url = new URL(mUri.toString());


            Log.v("mytag", "Built URI " + mUri.toString());

            // Open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) { 
                // Nothing to do.
                mListener.OnComplete("input stream equals null", NO_INPUT_STREAM_EXCEPTION, ERROR);
                Log.v("mytag", "input stream equals null: " + mUri.toString());
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            responseString = reader.readLine();

        } catch (IOException e) {
            mListener.OnComplete("IOException caught: " + e.toString(), IOEXCEPTION, ERROR);
            Log.v("mytag", "IOException caught: " + e.toString());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    mListener.OnComplete("IOException caught trying to close reader...: " + e.toString(), IOEXCEPTION, ERROR);
                    Log.v("mytag", "IOException caught trying to close reader...: " + e.toString());
                    return null;
                }
            }

        }
        return responseString;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(String result) {
        if(result==null){
            mListener.OnComplete("", NULL_RESULT, WARNING);
        }else{
            mListener.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }
}


