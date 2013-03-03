package berlin.reiche.securitas.controller;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.controller.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.controller.tasks.DetectionRequest;
import berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand;

public class DetectionState extends ControllerState<State> {

	/**
	 * Tag name for logging.
	 */
	private static final String TAG = DetectionState.class.getSimpleName();

	public DetectionState(Controller<State> controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {
		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case STOP_DETECTION:
			requestDetectionStop();
			break;
		case DOWNLOAD_SNAPSHOT:
			downloadSnapshot();
			break;
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		default:
			Log.e(TAG, "Illegal action request: " + request);
			throw new IllegalStateException();
		}
	}

	private void downloadSnapshot() {
		String uri = Client.endpoint;
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

	private void requestDetectionStop() {
		String uri = Client.endpoint + Protocol.STOP_DETECTION.operation;
		controller.setState(new IdleState(controller));
		new DetectionRequest(model, controller, DetectionCommand.STOP)
				.execute(uri);
	}
	
	private void downloadLatestSnapshot() {
		String uri = Client.endpoint;
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

}
