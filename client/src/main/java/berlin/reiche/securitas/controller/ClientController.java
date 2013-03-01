package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.ClientModel;

/**
 * Specific controller which is responsible for the client, respectively this
 * Android application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ClientController extends Controller {

	/**
	 * The specific model associated with this controller.
	 */
	private final ClientModel model;

	/**
	 * The current state of the controller.
	 */
	private ControllerState state;

	public ClientController(ClientModel model) {
		this.model = model;
		this.state = new IdleState(this);
	}

	protected void setState(ControllerState state) {
		if (this.state != null) {
			this.state.dispose();
		}
		this.state = state;
	}

	public ClientModel getModel() {
		return model;
	}

	@Override
	public void dispose() {
		super.dispose();
		state.dispose();
	}

	@Override
	void handleMessage(Message msg) {
		// TODO: check return value
		state.handleMessage(msg);
	}

}
