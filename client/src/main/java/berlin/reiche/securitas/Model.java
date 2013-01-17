package berlin.reiche.securitas;

import berlin.reiche.securitas.util.Observable;

public class Model extends Observable<Model> {

	enum State {
		DETECTING, IDLE;
	}

	private State state;

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
	
}
