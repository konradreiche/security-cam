package berlin.reiche.securitas;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    /**
     * Called when the device tries to register or unregister, but GCM returned
     * an error.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onError(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onError(Context arg0, String arg1) {
        // TODO Auto-generated method stub

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
    protected void onMessage(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * Called after a registration intent is received, passes the registration
     * ID assigned by GCM to that device/application pair as parameter.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onRegistered(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onRegistered(Context arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * Called after the device has been unregistered from GCM.
     * 
     * @see com.google.android.gcm.GCMBaseIntentService#onUnregistered(android.content.Context,
     *      java.lang.String)
     */
    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        // TODO Auto-generated method stub

    }

}
