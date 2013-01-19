package berlin.reiche.securitas.controller;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class Controller {

	final HandlerThread inboxHandlerThread;
	final Handler inboxHandler;
	final List<Handler> outboxHandlers;

	public Controller() {
		inboxHandlerThread = new HandlerThread("Controller Inbox");
		inboxHandlerThread.start();
		
		inboxHandler = new InboxHandler(inboxHandlerThread.getLooper(), this);
		outboxHandlers = new ArrayList<Handler>();
	}

	abstract void handleMessage(Message msg);

	/**
	 * Asks the inbox handler thread to shutdown gracefully.
	 */
	public void dispose() {
		inboxHandlerThread.getLooper().quit();
	}

	public final void addOutboxHandler(Handler handler) {
		outboxHandlers.add(handler);
	}

	public final void removeOutboxHandler(Handler handler) {
		outboxHandlers.remove(handler);
	}

	final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {

		if (!outboxHandlers.isEmpty()) {
			for (Handler handler : outboxHandlers) {
				Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
			}
		}
	}

	public Handler getInboxHandler() {
		return inboxHandler;
	}
	
	

}
