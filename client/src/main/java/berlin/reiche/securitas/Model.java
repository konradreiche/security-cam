package berlin.reiche.securitas;

import berlin.reiche.securitas.util.Observable;

public class Model extends Observable<Model> {

	public enum State {
		IDLE, WAIT_FOR_DETECTION_START, DETECTING, WAIT_FOR_DETECTION_STOP;
	}

	private State state;

	private String status;
	
	private boolean registeredOnServer;

	public Model() {
		super();
		state = State.IDLE;
	}

	public synchronized void setState(State state) {
		this.state = state;
		notifyObservers(this);
	}

	public synchronized State getState() {
		return state;
	}

	public synchronized String getStatus() {
		return status;
	}
	

	public boolean isRegisteredOnServer() {
		return registeredOnServer;
	}

	public synchronized void setStatus(String status) {
		this.status = status;
		notifyObservers(this);
	}

	public void onRequestFail() {
		switch (state) {
		case WAIT_FOR_DETECTION_START:
			setState(State.IDLE);
			break;
		case WAIT_FOR_DETECTION_STOP:
			setState(State.DETECTING);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void setRegisteredOnServer(boolean flag) {
		registeredOnServer = flag;
		notifyObservers(this);
	}

}
