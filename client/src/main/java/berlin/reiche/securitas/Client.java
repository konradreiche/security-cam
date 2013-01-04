package berlin.reiche.securitas;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import berlin.reiche.securitas.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.tasks.DetectionRequest;
import berlin.reiche.securitas.tasks.DeviceRegistration;
import berlin.reiche.securitas.tasks.DeviceRegistration.DeviceCommand;
import berlin.reiche.securitas.util.Settings;

public class Client {

	private static final String TAG = Client.class.getSimpleName();

	// TODO: define enum constant for REST operations

	static String endpoint;

	static Settings settings;

	static boolean detectionActive;

	public static void toggleMotionDetection(Activity activity) {
		if (!detectionActive) {
			startMotionDetection(activity);
		} else {
			stopMotionDetection(activity);
		}
	}

	public static void registerDevice(String id, Context context) {
		String operation = "/device/register";
		String uri = endpoint + operation;
		new DeviceRegistration(id, DeviceCommand.REGISTER, context)
				.execute(uri);
	}

	public static void unregisterDevice(String id, Context context) {
		String operation = "/device/unregister";
		String uri = endpoint + operation;
		new DeviceRegistration(id, DeviceCommand.UNREGISTER, context)
				.execute(uri);
	}

	private static void startMotionDetection(Activity activity) {
		String operation = "/motion/detection/start";
		String uri = endpoint + operation;
		new DetectionRequest(activity).execute(uri);
	}

	private static void stopMotionDetection(Activity activity) {
		String operation = "/motion/detection/stop";
		String uri = endpoint + operation;
		new DetectionRequest(activity).execute(uri);
	}

	public static void downloadLatestSnapshot(Activity activity,
			ImageView imageView) {
		String url = endpoint + "/server/action/snapshot";
		new BitmapDownloadTask(activity, imageView).execute(url);
	}

	public static void downloadSnapshot(Activity activity, ImageView imageView,
			String filename) {
		String url = endpoint + "/static/captures/" + filename;
		Log.d(TAG, url);
		new BitmapDownloadTask(activity, imageView).execute(url);
	}

	public static Settings getSettings() {
		return settings;
	}

	public static boolean isDetectionActive() {
		return detectionActive;
	}

	public static void toggleDetectionActive() {
		detectionActive = !detectionActive;
	}

}
