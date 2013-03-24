package berlin.reiche.securitas.model;

import android.graphics.Bitmap;
import berlin.reiche.securitas.model.ClientModel.State;

public class ClientModel extends Model<State> {

	public enum State {
		IDLE, DETECTING;
	}

	protected State previous;

	private boolean registeredOnServer;

	private Bitmap snapshot;

	public ClientModel() {
		super();
		state = State.IDLE;
	}

	public boolean isRegisteredOnServer() {
		return registeredOnServer;
	}

	@Override
	public synchronized void setState(Enum<State> state) {
		this.previous = (State) this.state;
		super.setState(state);
	}

	/**
	 * @return the previous state to which the model is set again.
	 */
	public State onRequestFail() {
		setState(previous);
		return previous;
	}

	public void setRegisteredOnServer(boolean flag) {
		registeredOnServer = flag;
	}

	public void setSnapshot(Bitmap snapshot) {
		this.snapshot = snapshot;
	}

	public Bitmap getSnapshot() {
		return snapshot;
	}

	public boolean isDetecting() {
		return state == State.DETECTING;
	}

}
