package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.model.Model;

/**
 * The controller state specifies, how the controller is reacting on certain
 * messages.
 * 
 * @author Konrad Reiche
 * 
 */
public abstract class ControllerState<T extends Enum<T>> {

	protected final Model<T> model;

	protected final Controller<T> controller;

	public ControllerState(Controller<T> controller) {
		this.controller = controller;
		this.model = controller.getModel();
	}

	public abstract void handleMessage(Message msg);
}
