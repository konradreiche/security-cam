package berlin.reiche.securitas.controller;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import berlin.reiche.securitas.Model;

/**
 * The controller state specifies, how the controller is reacting on certain
 * messages.
 * 
 * @author Konrad Reiche
 * 
 */
public abstract class ControllerState<T extends Enum<T>> {

	private final Handler handler;

	private final HandlerThread workerThread;

	protected final Model<T> model;

	protected final Controller<T> controller;

	public ControllerState(Controller<T> controller) {
		this.controller = controller;
		this.model = controller.getModel();
		this.handler = new Handler();
		this.workerThread = new HandlerThread("Controller State Worker Thread");
	}

	public void handleMessage(Message msg) {
		handler.handleMessage(msg);
	}

	public void dispose() {
		workerThread.getLooper().quit();
	}
}
