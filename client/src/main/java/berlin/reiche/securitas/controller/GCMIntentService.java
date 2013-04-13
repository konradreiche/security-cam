package berlin.reiche.securitas.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.activities.MainActivity;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * The GCM Intent Service is used to process received push notifications. For
 * now this means that a motion was detected and now the device is notified
 * about that.
 * 
 * @author Konrad Reiche
 * 
 */
public class GCMIntentService extends GCMBaseIntentService {

	/**
	 * Tag for logging.
	 */
	private static String TAG = GCMIntentService.class.getSimpleName();

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	static final String EXTRA_MESSAGE = "message";

	/**
	 * Identifies the notification system service for retrieving the object for
	 * it.
	 */
	private static final String NS = Context.NOTIFICATION_SERVICE;

	/**
	 * Unique identifier across different types of notifications.
	 */
	private static final int NOTIFICATION_ID = 1;

	/**
	 * Used to make sure that after a notification is selected no new activies
	 * are spawned.
	 */
	private static final int FLAGS = Intent.FLAG_ACTIVITY_SINGLE_TOP
			| Intent.FLAG_ACTIVITY_CLEAR_TOP;

	/**
	 * The number of motions until the first notification response.
	 */
	static volatile int motionsDetected = 0;

	/**
	 * Registers the device on the GCM service. If the device is already
	 * registered the cached registration ID will be used.
	 */
	public static void manageDeviceRegistration(Context context) {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String id = GCMRegistrar.getRegistrationId(context);
		if (id.equals("")) {
			Log.i(TAG, "No device id yet, issue registration indent.");
			String senderId = Client.getSettings().getGcmSenderId();
			GCMRegistrar.register(context, senderId);
		} else {
			Log.d(TAG, "Tell controller to register the id " + id);
			Client.getController().registerDevice(id);
		}
	}

	/**
	 * Used to reset the counter for the number of motions detected.
	 * 
	 * @param context
	 *            the context from which this method is invoked.
	 */
	public static void resetMotionsDetected(Context context) {
		motionsDetected = 0;
		((NotificationManager) context.getSystemService(NS)).cancelAll();
	}

	/**
	 * Default constructor.
	 */
	public GCMIntentService() {
		super();
	}

	/**
	 * Factory method for building the notification object.
	 * 
	 * @param timestamp
	 *            the time when the motion was detected.
	 * @param filename
	 *            name of the file storing the snapshot.
	 * @return the constructed notification object.
	 */
	private Notification createNotification(String timestamp, String filename) {

		String text = "Motion Alert";
		if (motionsDetected > 1) {
			text += " (" + motionsDetected + ")";
		}

		int icon = R.drawable.ic_stat;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, text, when);
		notification.defaults |= Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = timestamp;
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(FLAGS);

		notificationIntent.putExtra("filename", filename);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		return notification;
	}

	/**
	 * The sender ID is used in the registration process to identify this
	 * application as being permitted to send messages to the device.
	 */
	@Override
	protected String[] getSenderIds(Context context) {
		return new String[] { Client.getSettings().getGcmSenderId() };
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

		Notification notification = createNotification(timestamp, filename);
		nm.notify(NOTIFICATION_ID, notification);
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
		Client.getController().registerDevice(registrationId);
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
		Client.getController().unregisterDevice(registrationId);
	}

}
