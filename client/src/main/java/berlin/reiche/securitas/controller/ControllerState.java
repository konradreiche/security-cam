package berlin.reiche.securitas.controller;

import berlin.reiche.securitas.ClientModel;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class ControllerState {

	private final Handler handler;

	private final HandlerThread workerThread;
	
	protected final ClientModel model;
	
	protected final ClientController controller;

	public ControllerState(ClientController controller) {
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
