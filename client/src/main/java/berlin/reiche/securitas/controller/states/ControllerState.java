package berlin.reiche.securitas.controller.states;

import android.os.Message;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.model.Model;

/**
 * The controller state specifies, how the controller is reacting on certain
 * messages.
 * 
 * @author Konrad Reiche
 * 
 */
public abstract class ControllerState<T extends Enum<T>> {

	/**
	 * Reference on the model.
	 */
	protected final Model<T> model;

	/**
	 * Reference on the controller.
	 */
	protected final Controller<T> controller;

	/**
	 * Default constructor.
	 * 
	 * @param controller
	 *            controller to which this state belongs.
	 */
	public ControllerState(Controller<T> controller) {
		this.controller = controller;
		this.model = controller.getModel();
	}

	/**
	 * Handles the incoming message. When subclassing {@link ControllerState}
	 * this method has to be overridden in order to specify the actual behavior.
	 * 
	 * @param msg
	 *            the received message.
	 */
	public abstract void handleMessage(Message msg);
}
