package berlin.reiche.securitas;

import static berlin.reiche.securitas.MainActivity.SENDER_ID;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    private static String TAG = "security-cam";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";

    public GCMIntentService() {
        super(SENDER_ID);
    }

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
        
        String path = intent.getExtras().getString("picture");
        
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) getSystemService(ns);

        int icon = R.drawable.icon;
        CharSequence tickerText = "Alert!";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.defaults |= Notification.DEFAULT_ALL;
        
        context = getApplicationContext();
        CharSequence contentTitle = "Motion Alert!";
        CharSequence contentText = "Motion Alert!";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        nm.notify(1, notification);
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
        ServerUtilities.registerDevice(context, registrationId);
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
        ServerUtilities.unregisterDevice(context, registrationId);
    }

}
