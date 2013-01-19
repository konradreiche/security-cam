package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.Model.State;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.tasks.DetectionRequest;
import berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand;

/**
 * IdleState is one of different controller states.
 * 
 * @author Konrad Reiche
 * 
 */
public class IdleState extends ControllerState {

	public IdleState(ClientController controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {

		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case START_DETECTION:
			requestDetectionStart();
			model.setState(State.WAIT_FOR_DETECTION_START);
			break;
		case STOP_DETECTION:
			requestDetectionStop();
			model.setState(State.WAIT_FOR_DETECTION_STOP);
		}
	}

	private void requestDetectionStart() {
		String uri = Client.endpoint + Protocol.START_DETECTION.operation;
		Model model = controller.getModel();
		new DetectionRequest(DetectionCommand.START, model).execute(uri);
	}

	private void requestDetectionStop() {
		String uri = Client.endpoint + Protocol.STOP_DETECTION.operation;
		Model model = controller.getModel();
		new DetectionRequest(DetectionCommand.STOP, model).execute(uri);
	}

}
