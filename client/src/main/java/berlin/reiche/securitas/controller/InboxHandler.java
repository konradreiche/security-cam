package berlin.reiche.securitas.controller;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Inbox handler for implementing the infrastructure of a message oriented
 * observer pattern.
 * 
 * @author Konrad Reiche
 * 
 */
final class InboxHandler extends Handler {

	/**
	 * The controller to which this inbox handler is associated to. The generic
	 * type does not matter on this abstraction level.
	 */
	Controller<?> controller;

	/**
	 * Default constructor.
	 * 
	 * @param looper
	 *            Looper which used to run the message loop.
	 * @param controller
	 *            the controller to which this inbox handler is associated to.
	 */
	InboxHandler(Looper looper, Controller<?> controller) {
		super(looper);
		this.controller = controller;
	}

	/**
	 * Delegates the message to the controller's handle message method.
	 */
	@Override
	public void handleMessage(Message msg) {
		controller.handleMessage(msg);
	}
}
