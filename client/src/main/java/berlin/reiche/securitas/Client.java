package berlin.reiche.securitas;

import static berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand.START;
import static berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand.STOP;
import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import berlin.reiche.securitas.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.tasks.DetectionRequest;
import berlin.reiche.securitas.tasks.DeviceRegistration;
import berlin.reiche.securitas.tasks.DeviceRegistration.DeviceCommand;
import berlin.reiche.securitas.tasks.StatusTask;
import berlin.reiche.securitas.util.Settings;

public class Client {

	// TODO: define enum constant for REST operations

	static String endpoint;

	static Settings settings;

	static boolean detectionActive;
	
	static MainActivity activity;

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
		new DetectionRequest(activity, START).execute(uri);
	}

	private static void stopMotionDetection(Activity activity) {
		String operation = "/motion/detection/stop";
		String uri = endpoint + operation;
		new DetectionRequest(activity, STOP).execute(uri);
	}

	public static void downloadLatestSnapshot(Activity activity,
			ImageView imageView) {
		String url = endpoint + "/server/action/snapshot";
		new BitmapDownloadTask(activity, imageView).execute(url);
	}

	public static void downloadSnapshot(Activity activity, ImageView imageView,
			String filename) {
		String url = endpoint + "/static/captures/" + filename;
		new BitmapDownloadTask(activity, imageView).execute(url);
	}
	
	public static void retrieveServerStatus(Activity activity) {
		String url = endpoint + "/server/status";
		new StatusTask(activity).execute(url);
	}

	public static Settings getSettings() {
		return settings;
	}

	public static boolean isDetectionActive() {
		return detectionActive;
	}

	public static void toggleDetectionActive() {
		detectionActive = !detectionActive;
		if (detectionActive) {
			activity.enableDetectionUI();
		} else {
			activity.disableDetectionUI();
		}
	}

}
