package berlin.reiche.securitas.controller;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class ControllerState {

	private final Handler handler;

	private final HandlerThread workerThread;

	public ControllerState() {
		handler = new Handler();
		workerThread = new HandlerThread("Controller State Worker Thread");
	}

	public void handleMessage(Message msg) {
		handler.handleMessage(msg);
	}

	public void dispose() {
		workerThread.getLooper().quit();
	}
}
