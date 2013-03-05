package berlin.reiche.securitas.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.activies.MainActivity;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static String TAG = GCMIntentService.class.getSimpleName();

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	static final String EXTRA_MESSAGE = "message";

	private static final String NS = Context.NOTIFICATION_SERVICE;

	/**
	 * The number of motions until the first notification response.
	 */
	static volatile int motionsDetected = 0;

	public GCMIntentService() {
		super(MainActivity.GCM_SENDER_ID);
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

		motionsDetected++;
		Log.i(TAG, "onMessage, intent = " + intent.getDataString());

		NotificationManager nm = (NotificationManager) getSystemService(NS);
		String timestamp = intent.getExtras().getString("timestamp");
		String filename = intent.getExtras().getString("filename");

		Log.i(TAG, "Received filename " + filename);

		String text = "Motion Alert";
		if (motionsDetected > 1) {
			text += " (" + motionsDetected + ")";
		}

		int icon = R.drawable.icon;
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.defaults |= Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = timestamp;
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);

		notificationIntent.putExtra("filename", filename);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
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
		Handler handler = Client.getController().getInboxHandler();
		handler.sendMessage(Message.obtain(handler,
				Protocol.REGISTER_DEVICE.code, registrationId));
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
		Handler handler = Client.getController().getInboxHandler();
		handler.sendMessage(Message.obtain(handler,
				Protocol.UNREGISTER_DEVICE.code, registrationId));
	}

	public static void resetMotionsDetected(Context context) {
		motionsDetected = 0;
		((NotificationManager) context.getSystemService(NS)).cancelAll();
	}

}
