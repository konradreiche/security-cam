package berlin.reiche.securitas.controller;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.ClientModel.State;
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
		default:
			Log.e(TAG, "Illegal action request: " + request);
			throw new IllegalStateException();
		}
	}

	private void requestDetectionStop() {
		String uri = Client.endpoint + Protocol.STOP_DETECTION.operation;
		controller.setState(new IdleState(controller));
		new DetectionRequest(model, controller, DetectionCommand.STOP)
				.execute(uri);
	}

}
