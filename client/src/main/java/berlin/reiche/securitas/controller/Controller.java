package berlin.reiche.securitas.controller;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import berlin.reiche.securitas.controller.states.ControllerState;
import berlin.reiche.securitas.model.Model;

/**
 * Generic controller for implementing the MVC like Android application
 * architecture.
 * 
 * @author Konrad Reiche
 * 
 * @param <T>
 *            {@link Enum} type of the model state.
 */
public abstract class Controller<T extends Enum<T>> {

	/**
	 * The model of this application.
	 */
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

	/**
	 * Number of outbox handlers used to notify different interfaces.
	 */
	final List<Handler> outboxHandlers;

	/**
	 * The current state of the controller.
	 */
	protected ControllerState<T> state;

	/**
	 * Default constructor which initializes the handlers and their threads.
	 */
	public Controller() {
		inboxHandlerThread = new HandlerThread("Controller Inbox");
		inboxHandlerThread.start();

		inboxHandler = new InboxHandler(inboxHandlerThread.getLooper(), this);
		outboxHandlers = new ArrayList<Handler>();
	}

	/**
	 * Adds a new handler for receiving model state changes.
	 * 
	 * @param handler
	 *            a handler to process messages.
	 */
	public final void addOutboxHandler(Handler handler) {
		outboxHandlers.add(handler);
	}

	/**
	 * Asks the inbox handler thread to shutdown gracefully.
	 */
	public final void dispose() {
		inboxHandlerThread.getLooper().quit();
	}

	/**
	 * @return the inbox handler.
	 */
	public Handler getInboxHandler() {
		return inboxHandler;
	}

	/**
	 * @return the model associated with this controller
	 */
	public Model<T> getModel() {
		return model;
	}

	/**
	 * Needs to be overridden by the specific controller in order to react to
	 * different messages.
	 * 
	 * @param msg
	 *            the message received.
	 */
	abstract void handleMessage(Message msg);

	/**
	 * Notifies the handlers registered with the outbox.
	 * 
	 * @param what
	 *            what kind of notification, coded as integer value.
	 */
	public final void notifyOutboxHandlers(int what) {
		notifyOutboxHandlers(what, 0, 0, null);
	}

	/**
	 * Iterates all handlers and sends the message to their respective target.
	 * 
	 * @param what
	 *            what kind of notification, coded as integer value.
	 * @param arg1
	 *            additional parameter.
	 * @param arg2
	 *            another additional parameter.
	 * @param obj
	 *            yet another additional parameter, but storing more complex
	 *            information.
	 */
	public final void notifyOutboxHandlers(int what, int arg1, int arg2,
			Object obj) {
		if (!outboxHandlers.isEmpty()) {
			for (Handler handler : outboxHandlers) {
				Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
			}
		}
	}

	/**
	 * Notifies the handlers registered with the outbox and passes an additional
	 * {@link Object} which stores information.
	 * 
	 * @param what
	 *            what kind of notification, coded as integer value.
	 * @param obj
	 *            object storing additional information
	 */
	public final void notifyOutboxHandlers(int what, Object obj) {
		notifyOutboxHandlers(what, 0, 0, obj);
	}

	/**
	 * Removes a handler which was added before so certain interfaces do not get
	 * updated anymore.
	 * 
	 * @param handler
	 */
	public final void removeOutboxHandler(Handler handler) {
		outboxHandlers.remove(handler);
	}

	/**
	 * Updates the controller to a new state in order to change the message
	 * processing.
	 * 
	 * @param state
	 *            the new controller's state.
	 */
	public void setState(ControllerState<T> state) {
		this.state = state;
	}

}
