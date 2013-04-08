package berlin.reiche.securitas.model;

/**
 * Representation of a generic model class as used in a MVC like architecture.
 * 
 * @author Konrad Reiche
 * 
 * @param <T>
 *            {@link Enum} type of the model state.
 */
public abstract class Model<T extends Enum<T>> {

	/**
	 * Current state of the model.
	 */
	protected Enum<T> state;

	/**
	 * @return current state of the model.
	 */
	public synchronized Enum<T> getState() {
		return state;
	}

	/**
	 * @param state
	 *            new state the model should transition to.
	 */
	public synchronized void setState(Enum<T> state) {
		this.state = state;
	}

}
