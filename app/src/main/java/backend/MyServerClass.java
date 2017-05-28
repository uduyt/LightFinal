package backend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MyServerClass extends AsyncTask<Object, String, ListenerArguments> {

    private HttpURLConnection urlConnection;
    private Uri mUri;
    private OnTaskCompletedListener mListener;
    private String responseString = "null";
    private ExceptionHandler mExceptionHandler;
    private boolean mSendError = true;
    private Context mContext;

    public static final int NO_INPUT_STREAM_EXCEPTION = 1;
    public static final int IOEXCEPTION = 2;
    public static final int NOT_CONNECTED = 4;
    public static final int SHOULDNT_HAPPEN = 5;
    public static final int NULL_RESULT = 6;

    public static final int ERROR = -1;
    public static final int WARNING = -2;
    public static final int SUCCESSFUL = -3;

    public MyServerClass(Context context) {
        mContext = context;
    }

    public MyServerClass(Context context, boolean sendError) {
        mContext = context;
        mSendError = false;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public void setListener(OnTaskCompletedListener listener) {
        mListener = listener;
    }

    @Override
    protected ListenerArguments doInBackground(Object... params) {

        BufferedReader reader = null;

        try {

            if (!isConnected(mContext)) {
                Log.v("mytag", "not connected to the internet");
                return new ListenerArguments("not connected to the internet", NOT_CONNECTED, WARNING);
            }
        } catch (Exception e) {

            return new ListenerArguments("isConnected ha dado el siguiente error: " + e.toString(), SHOULDNT_HAPPEN, ERROR);

        }

        try {


            URL url = new URL(mUri.toString());


            Log.v("mytag", "Built URI " + mUri.toString());

            // Open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                Log.v("mytag", "input stream equals null: " + mUri.toString());
                return new ListenerArguments("input stream equals null", NO_INPUT_STREAM_EXCEPTION, ERROR);
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            responseString = reader.readLine();

            urlConnection.disconnect();
            reader.close();

        } catch (java.net.SocketException e) {
            return new ListenerArguments("not connected to the internet", NOT_CONNECTED, WARNING);
        } catch (java.net.SocketTimeoutException e) {
            return new ListenerArguments("not connected to the internet", NOT_CONNECTED, WARNING);
        } catch (java.net.UnknownHostException e) {
            return new ListenerArguments("not connected to the internet", NOT_CONNECTED, WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListenerArguments("Exception caught: " + e.toString(), IOEXCEPTION, ERROR);

        }

        return new ListenerArguments(responseString, SUCCESSFUL, SUCCESSFUL);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(ListenerArguments result) {
        if (result.getResultType() == MyServerClass.ERROR) {
            Log.v("mytag", "error: " + result.getResult());
            if (mSendError)
                new ExceptionHandler(mContext, result.getResult()).execute();
        }
        mListener.OnComplete(result.getResult(), result.getResultCode(), result.getResultType());
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();


        if(activeNetwork != null){
            if(activeNetwork.isConnectedOrConnecting()){
                return true;
            }else{
                Log.v("mytag", "not connected because of network info");
                return false;
            }
        }

        Log.v("mytag", "not connected because network info is null");
        return false;


    }
}


