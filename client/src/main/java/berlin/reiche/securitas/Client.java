package berlin.reiche.securitas;

import android.content.Context;
import android.widget.ImageView;
import berlin.reiche.securitas.activies.MainActivity;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.tasks.DetectionRequest;
import berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand;
import berlin.reiche.securitas.tasks.DeviceRegistration;
import berlin.reiche.securitas.tasks.DeviceRegistration.DeviceCommand;
import berlin.reiche.securitas.tasks.StatusTask;
import berlin.reiche.securitas.util.Settings;

/**
 * The model of this application.
 * 
 * @author Konrad Reiche
 * 
 */
public class Client {

	public static String endpoint;

	public static Settings settings;

	public static boolean motionDetectionActive;

	public static MainActivity activity;

	private static Model model;

	private static Controller controller;

	static {
		model = new Model();
		controller = new ClientController(model);
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

	public static void invokeDetectionStart() {
		activity.lockInterface();
		String operation = "/motion/detection/start";
		String uri = endpoint + operation;
		new DetectionRequest(activity, DetectionCommand.START).execute(uri);
	}

	public static void invokeDetectionStop() {
		activity.lockInterface();
		String operation = "/motion/detection/stop";
		String uri = endpoint + operation;
		new DetectionRequest(activity, DetectionCommand.STOP).execute(uri);
	}

	public static void downloadLatestSnapshot(ImageView imageView) {
		activity.lockInterface();
		String url = endpoint + "/server/action/snapshot";
		new BitmapDownloadTask(activity, imageView).execute(url);
	}

	public static void downloadSnapshot(ImageView imageView, String filename) {
		String url = endpoint + "/static/captures/" + filename;
		new BitmapDownloadTask(activity, imageView).execute(url);
	}

	public static void synchronizeServerStatus() {
		activity.lockInterface();
		String url = endpoint + "/server/status";
		new StatusTask(activity).execute(url);
	}

	public static Settings getSettings() {
		return settings;
	}

	public static boolean isMotionDetectionActive() {
		return motionDetectionActive;
	}

	public static void restoreClientState(boolean motionDetectionActive) {
		if (Client.motionDetectionActive != motionDetectionActive) {
			Client.motionDetectionActive = motionDetectionActive;
		}
		activity.reflectClientState();
	}

	public static void enableMotionDetection() {
		Client.motionDetectionActive = true;
		activity.triggerInterfaceUpdate();
	}

	public static void disableMotionDetection() {
		Client.motionDetectionActive = false;
		activity.triggerInterfaceUpdate();
	}

	public static Model getModel() {
		return model;
	}

	public static Controller getController() {
		return controller;
	}

}
