package berlin.reiche.securitas.controller.states;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.tasks.DetectionRequest;
import berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand;
import berlin.reiche.securitas.controller.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.controller.tasks.DeviceRegistration;
import berlin.reiche.securitas.controller.tasks.StatusTask;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;

/**
 * IdleState is one of different controller states.
 * 
 * @author Konrad Reiche
 * 
 */
public class IdleState extends ControllerState<State> {

	private static final String TAG = IdleState.class.getSimpleName();

	/**
	 * Hides the super class controller field in order to avoid type casting.
	 */
	ClientController controller;

	public IdleState(ClientController controller) {
		super(controller);
		this.controller = controller;
	}

	@Override
	public void handleMessage(Message msg) {

		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case START_DETECTION:
			requestDetectionStart();
			break;
		case RESTORE_CLIENT_STATE:
			restoreClientState((String) msg.obj);
			break;
		case REGISTER_DEVICE:
			registerDevice(msg.obj.toString());
			break;
		case UNREGISTER_DEVICE:
			unregisterDevice(msg.obj.toString());
			break;
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		case DOWNLOAD_MOTION_SNAPSHOT:
			downloadMotionSnapshot(msg.obj.toString());
			break;
		default:
			Log.e(TAG, "Illegal protocol request: " + request);
			throw new IllegalStateException();
		}
	}

	private void downloadLatestSnapshot() {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

	private void downloadMotionSnapshot(String filename) {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_MOTION_SNAPSHOT.operation + filename;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

	private void restoreClientState(String motionFilename) {
		String uri = Client.getEndpoint()
				+ Protocol.RESTORE_CLIENT_STATE.operation;
		new StatusTask(model, controller, motionFilename).execute(uri);
	}

	private void requestDetectionStart() {
		String uri = Client.getEndpoint() + Protocol.START_DETECTION.operation;
		new DetectionRequest(model, controller, DetectionCommand.START)
				.execute(uri);
	}

	private void registerDevice(String id) {
		String uri = Client.getEndpoint() + Protocol.REGISTER_DEVICE.operation;
		new DeviceRegistration(model, controller, id,
				DeviceRegistration.Command.REGISTER).execute(uri);
	}

	private void unregisterDevice(String id) {
		String uri = Client.getEndpoint()
				+ Protocol.UNREGISTER_DEVICE.operation;
		new DeviceRegistration(model, controller, id,
				DeviceRegistration.Command.UNREGISTER).execute(uri);
	}
}
