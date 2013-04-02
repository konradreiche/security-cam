package berlin.reiche.securitas.controller;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import berlin.reiche.securitas.controller.states.ControllerState;
import berlin.reiche.securitas.model.Model;

public abstract class Controller<T extends Enum<T>> {

	Model<T> model;

	/**
	 * Inbox handler receives messages from the activity and its {@link Looper}
	 * processes the messages.
	 */
	final Handler inboxHandler;

	/**
	 * Thread for the inbox handler that contains already a {@link Looper}.
	 */
	final HandlerThread inboxHandlerThread;
	final List<Handler> outboxHandlers;

	/**
	 * The current state of the controller.
	 */
	protected ControllerState<T> state;

	public Controller() {
		inboxHandlerThread = new HandlerThread("Controller Inbox");
		inboxHandlerThread.start();

		inboxHandler = new InboxHandler(inboxHandlerThread.getLooper(), this);
		outboxHandlers = new ArrayList<Handler>();
	}

	/**
	 * Needs to be overridden by the specific controller in order to react to
	 * different messages.
	 * 
	 * @param msg
	 *            the message received.
	 */
	abstract void handleMessage(Message msg);

	public void setState(ControllerState<T> state) {
		this.state = state;
	}

	/**
	 * Asks the inbox handler thread to shutdown gracefully.
	 */
	public final void dispose() {
		inboxHandlerThread.getLooper().quit();
	}

	public final void addOutboxHandler(Handler handler) {
		outboxHandlers.add(handler);
	}

	public final void removeOutboxHandler(Handler handler) {
		outboxHandlers.remove(handler);
	}

	public final void notifyOutboxHandlers(int what) {
		notifyOutboxHandlers(what, 0, 0, null);
	}

	public final void notifyOutboxHandlers(int what, Object obj) {
		notifyOutboxHandlers(what, 0, 0, obj);
	}

	public final void notifyOutboxHandlers(int what, int arg1, int arg2,
			Object obj) {
		if (!outboxHandlers.isEmpty()) {
			for (Handler handler : outboxHandlers) {
				Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
			}
		}
	}

	public Handler getInboxHandler() {
		return inboxHandler;
	}

	public Model<T> getModel() {
		return model;
	}

}
