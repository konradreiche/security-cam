package berlin.reiche.securitas;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    private static String TAG = "security-cam";

    /**
     * Called when the device tries to register or unregister, but GCM returned
     * an error.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onError(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onError(Context context, String errorId) {
        Log.e(TAG, "onError, errorId = " + errorId);
    }

    /**
     * Called when your server sends a message to GCM, and GCM delivers it to
     * the device. If the message has a payload, its contents are available as
     * extras in the intent.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onMessage(android.content.Context,
     *      android.content.Intent)
     */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "onMessage, intent = " + intent.getDataString());
    }

    /**
     * Called after a registration intent is received, passes the registration
     * ID assigned by GCM to that device/application pair as parameter.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onRegistered(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "onRegistered, registrationId = " + registrationId);
    }

    /**
     * Called after the device has been unregistered from GCM.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onUnregistered(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "onUnregistered, registrationId = " + registrationId);

    }

}
