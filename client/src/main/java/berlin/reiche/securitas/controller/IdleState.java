package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.tasks.DetectionRequest;
import berlin.reiche.securitas.tasks.DeviceRegistration;
import berlin.reiche.securitas.tasks.StatusTask;
import berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand;

/**
 * IdleState is one of different controller states.
 * 
 * @author Konrad Reiche
 * 
 */
public class IdleState extends ControllerState<ClientModel.State> {

	public IdleState(ClientController controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {

		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case START_DETECTION:
			requestDetectionStart();
			model.setState(State.IDLE);
			break;
		case STOP_DETECTION:
			requestDetectionStop();
			model.setState(State.WAIT);
			break;
		case RESTORE_CLIENT_STATE:
			restoreClientState();
			break;
		case REGISTER_DEVICE:
			registerDevice(msg.obj.toString());
			break;
		case UNREGISTER_DEVICE:
			unregisterDevice(msg.obj.toString());
		default:
			throw new IllegalStateException();
		}
	}

	private void restoreClientState() {
		String uri = Client.endpoint + Protocol.RESTORE_CLIENT_STATE.operation;
		new StatusTask(model).execute(uri);
	}

	private void requestDetectionStart() {
		String uri = Client.endpoint + Protocol.START_DETECTION.operation;
		new DetectionRequest(DetectionCommand.START, model).execute(uri);
	}

	private void requestDetectionStop() {
		String uri = Client.endpoint + Protocol.STOP_DETECTION.operation;
		new DetectionRequest(DetectionCommand.STOP, model).execute(uri);
	}

	private void registerDevice(String id) {
		String uri = Client.endpoint + Protocol.REGISTER_DEVICE.operation;
		new DeviceRegistration(model, controller, id,
				DeviceRegistration.Command.REGISTER).execute(uri);
	}

	private void unregisterDevice(String id) {
		String uri = Client.endpoint + Protocol.UNREGISTER_DEVICE.operation;
		new DeviceRegistration(model, controller, id,
				DeviceRegistration.Command.UNREGISTER).execute(uri);
	}
}
