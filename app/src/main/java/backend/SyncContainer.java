package backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;
import com.witcode.light.light.MarketItem;

public class SyncContainer extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    public static final int SYNCING = 1;
    public static final int SYNCED = 2;
    public static final int SYNC_END = 3;
    public static final int SYNC_START_END = 4;
    public static final int JUST_SYNCED = 5;
    public static final int ERROR = 6;

    public SyncContainer(Context context, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;

        SetUp();
    }

    private void SetUp() {
        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/recycle/sync_user.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("user_id", Profile.getCurrentProfile().getId())
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
            switch (result){
                case "syncing":
                    mCallback.OnComplete(result, SyncContainer.SYNCING, SUCCESSFUL);
                    break;
                case "synced":
                    mCallback.OnComplete(result, SyncContainer.SYNCED, SUCCESSFUL);
                    break;
                case "sync_end":
                    mCallback.OnComplete(result, SyncContainer.SYNC_END, SUCCESSFUL);
                    break;
                case "sync_start_end":
                    mCallback.OnComplete(result, SyncContainer.SYNC_END, SUCCESSFUL);
                    break;
                case "just_synced":
                    mCallback.OnComplete(result, SyncContainer.SYNCED, SUCCESSFUL);
                    break;
                case "self_started_sync":
                    mCallback.OnComplete(result, SyncContainer.SYNCING, SUCCESSFUL);
                    break;
                default:
                    mCallback.OnComplete(result, SyncContainer.ERROR, SUCCESSFUL);
                    break;
            }
        }

    }

}




