package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.model.ClientModel;

/**
 * Specific controller which is responsible for the client, respectively this
 * Android application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ClientController extends Controller<ClientModel.State> {

	public ClientController(ClientModel model) {
		this.model = model;
		this.setState(new IdleState(this));
	}

	@Override
	void handleMessage(Message msg) {
		state.handleMessage(msg);
	}

}
