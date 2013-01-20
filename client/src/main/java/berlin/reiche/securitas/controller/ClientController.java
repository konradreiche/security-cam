package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.ClientModel;

public class ClientController extends Controller {

	private final ClientModel model;

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
