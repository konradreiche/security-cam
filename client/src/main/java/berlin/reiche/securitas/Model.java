package berlin.reiche.securitas;

import berlin.reiche.securitas.util.Observable;

public abstract class Model<T extends Enum<T>> extends
		Observable<Model<T>> {

	protected Enum<T> state;

	protected Enum<T> previous;
	
	public synchronized Enum<T> getState() {
		return state;
	}
	
	public synchronized void setState(Enum<T> state) {
		this.previous = this.state;
		this.state = state;
		notifyObservers(this);
	}

}
