package berlin.reiche.securitas.model;


public abstract class Model<T extends Enum<T>> {

	protected Enum<T> state;
	
	public synchronized Enum<T> getState() {
		return state;
	}
	
	public synchronized void setState(Enum<T> state) {
		this.state = state;
	}

}
