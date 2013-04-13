package berlin.reiche.securitas.controller;

import android.graphics.Bitmap;
import android.os.Message;
import berlin.reiche.securitas.activities.Action;
import berlin.reiche.securitas.controller.states.ControllerState;
import berlin.reiche.securitas.controller.states.IdleState;
import berlin.reiche.securitas.controller.tasks.StatusTask;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Protocol;

/**
 * Specific controller which is responsible for the client, respectively this
 * Android application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ClientController extends Controller<ClientModel.State> {

	/**
	 * Default constructor.
	 * 
	 * @param model
	 *            model of this application.
	 */
	public ClientController(ClientModel model) {
		this.model = model;
		this.setState(new IdleState(this));
	}

	/**
	 * Requests and sets a current snapshot.
	 */
	public void downloadLatestSnapshot() {
		int what = Protocol.DOWNLOAD_LATEST_SNAPSHOT.code;
		inboxHandler.sendEmptyMessage(what);
	}

	/**
	 * Requests and sets a specific snapshot.
	 * 
	 * @param motionFilename
	 *            the name of the file in which the motion snapshot is stored.
	 */
	public void downloadMotionSnapshot(String motionFilename) {
		int what = Protocol.DOWNLOAD_MOTION_SNAPSHOT.code;
		Message message = Message.obtain(inboxHandler, what, motionFilename);
		inboxHandler.sendMessage(message);
	}
	
	/**
	 * Delegates the message handling to the respective {@link ControllerState}.
	 */
	@Override
	void handleMessage(Message msg) {
		state.handleMessage(msg);
	}

	/**
	 * Used when there is no device registered with the endpoint. That happens
	 * when the status task retrieves {@link StatusTask.ServerStatus#IDLE} from
	 * the server. Then this method is used in order to register the device with
	 * the server so the server becomes {@link StatusTask.ServerStatus#READY}.
	 */
	public void issueDeviceRegistration() {
		int what = Action.ISSUE_DEVICE_REGISTRATION.code;
		notifyOutboxHandlers(what);
	}

	/**
	 * Registers this device on the endpoint.
	 * 
	 * @param id
	 *            unique identifier for this device.
	 */
	public void registerDevice(String id) {
		int what = Protocol.REGISTER_DEVICE.code;
		Message message = Message.obtain(inboxHandler, what, id);
		inboxHandler.sendMessage(message);
	}

	/**
	 * Sends an error message to the interface.
	 * 
	 * @param message
	 *            the message describing the error.
	 */
	public void reportError(String message) {
		notifyOutboxHandlers(Action.REPORT_ERROR.code, message);
	}

	/**
	 * Synchronized the client state with the server state.
	 */
	public void restoreClientState() {
		restoreClientState(null);
	}

	/**
	 * Synchronized the client state with the server state.
	 * 
	 * @param motionFilename
	 *            filename of the snapshot that triggered the motion event or
	 *            <code>null</code> if there was no motion event.
	 */
	public void restoreClientState(String motionFilename) {
		int what = Protocol.RESTORE_CLIENT_STATE.code;
		Message message = Message.obtain(inboxHandler, what, motionFilename);
		inboxHandler.sendMessage(message);
	}

	/**
	 * Notifies the interface to change to detection mode.
	 */
	public void setDetectionMode() {
		notifyOutboxHandlers(Action.SET_DETECTION_MODE.code);
	}

	/**
	 * Notifies the interface to change to idle mode.
	 */
	public void setIdleMode() {
		notifyOutboxHandlers(Action.SET_IDLE_MODE.code);
	}

	/**
	 * Notifies the interface about a change of the GCM registration state.
	 * 
	 * @param flag
	 *            whether or not the device is registered on the server.
	 */
	public void setRegisteredOnServer(boolean flag) {
		notifyOutboxHandlers(Action.SET_REGISTERED_ON_SERVER.code, flag);
	}

	/**
	 * Sends a bitmap to the interface for updating the current snapshot.
	 * 
	 * @param snapshot
	 *            the bitmap of the snapshot.
	 */
	public void setSnapshot(Bitmap snapshot) {
		notifyOutboxHandlers(Action.SET_SNAPSHOT.code, snapshot);
	}

	/**
	 * Requests to start the motion detection.
	 */
	public void startDetection() {
		inboxHandler.sendEmptyMessage(Protocol.START_DETECTION.code);
	}

	/**
	 * Requests to stop the motion detection.
	 */
	public void stopDetection() {
		inboxHandler.sendEmptyMessage(Protocol.STOP_DETECTION.code);
	}

	/**
	 * Unlocks the interface. Used after a task was processed.
	 * 
	 * @param detecting
	 *            whether or not the motion detection is active.
	 */
	public void unlockInterface(boolean detecting) {
		int what = Action.UNLOCK_INTERFACE.code;
		notifyOutboxHandlers(what, detecting);
	}

	/**
	 * Has to be called after device has been unregistered from GCM.
	 * 
	 * @param id
	 *            the device's identifier.
	 */
	public void unregisterDevice(String id) {
		int what = Protocol.UNREGISTER_DEVICE.code;
		Message message = Message.obtain(inboxHandler, what, id);
		inboxHandler.sendMessage(message);
	}

}
