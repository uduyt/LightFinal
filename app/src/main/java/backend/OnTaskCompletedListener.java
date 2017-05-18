package backend;

/**
 * Created by carlo on 09/03/2017.
 */

public interface OnTaskCompletedListener {

    void OnComplete(String result, int resultCode, int resultType);
}
