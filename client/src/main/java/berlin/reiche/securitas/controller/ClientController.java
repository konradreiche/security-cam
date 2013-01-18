package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Model;

public class ClientController extends Controller {

	private final Model model;

	private ControllerState state;
	
	public ClientController(Model model) {
		this.model = model;
		this.state = new IdleState();
	}
	
	protected void setState(ControllerState state) {
		if (this.state != null) {
			this.state.dispose();
		}
		this.state = state;
	}

	public Model getModel() {
		return model;
	}

	@Override
	void dispose() {
		super.dispose();
		state.dispose();
	}
	
	@Override
	void handleMessage(Message msg) {
		// TODO: check return value
		state.handleMessage(msg);
	}
	
}
