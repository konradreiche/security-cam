package berlin.reiche.securitas;

import berlin.reiche.securitas.ClientModel.State;

public class ClientModel extends Model<State>  {

	public enum State {
		IDLE, WAIT, DETECTING;
	}

	private String status;

	private boolean registeredOnServer;

	
	public ClientModel() {
		super();
		state = State.IDLE;
	}

	public synchronized String getStatus() {
		return status;
	}

	public boolean isRegisteredOnServer() {
		return registeredOnServer;
	}
	
	public void onRequestFail() {
		setState(previous);
	}


	public void setRegisteredOnServer(boolean flag) {
		registeredOnServer = flag;
		notifyObservers(this);
	}

	public synchronized void setStatus(String status) {
		this.status = status;
		notifyObservers(this);
	}

}
