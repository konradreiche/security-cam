package berlin.reiche.securitas.controller;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.controller.tasks.DetectionRequest;
import berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand;
import berlin.reiche.securitas.controller.tasks.DeviceRegistration;
import berlin.reiche.securitas.controller.tasks.StatusTask;

/**
 * IdleState is one of different controller states.
 * 
 * @author Konrad Reiche
 * 
 */
public class IdleState extends ControllerState<State> {

	private static final String TAG = IdleState.class.getSimpleName();

	public IdleState(Controller<State> controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {

		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case START_DETECTION:
			requestDetectionStart();
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
		case DOWNLOAD_SNAPSHOT:
			// swallow
			break;
		default:
			Log.e(TAG, "Illegal protocol request: " + request);
			throw new IllegalStateException();
		}
	}

	private void restoreClientState() {
		String uri = Client.endpoint + Protocol.RESTORE_CLIENT_STATE.operation;
		new StatusTask(model, controller).execute(uri);
	}

	private void requestDetectionStart() {
		String uri = Client.endpoint + Protocol.START_DETECTION.operation;
		controller.setState(new WaitState(controller));
		new DetectionRequest(model, controller, DetectionCommand.START)
				.execute(uri);
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
