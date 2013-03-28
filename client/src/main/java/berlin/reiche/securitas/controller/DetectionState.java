package berlin.reiche.securitas.controller;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.controller.tasks.DetectionRequest;
import berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;

public class DetectionState extends ControllerState<State> {

	/**
	 * Tag name for logging.
	 */
	private static final String TAG = DetectionState.class.getSimpleName();

	/**
	 * Hides the super class controller field in order to avoid type casting.
	 */
	ClientController controller;

	public DetectionState(ClientController controller) {
		super(controller);
		this.controller = controller;
	}

	@Override
	public void handleMessage(Message msg) {
		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case STOP_DETECTION:
			requestDetectionStop();
			break;
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		case DOWNLOAD_MOTION_SNAPSHOT:
			downloadMotionSnapshot(msg.obj.toString());
			break;
		default:
			Log.e(TAG, "Illegal action request: " + request);
			throw new IllegalStateException();
		}
	}

	private void downloadMotionSnapshot(String filename) {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_MOTION_SNAPSHOT.operation + filename;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

	private void requestDetectionStop() {
		String uri = Client.getEndpoint() + Protocol.STOP_DETECTION.operation;
		controller.setState(new IdleState(controller));
		new DetectionRequest(model, controller, DetectionCommand.STOP)
				.execute(uri);
	}

	private void downloadLatestSnapshot() {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

}
