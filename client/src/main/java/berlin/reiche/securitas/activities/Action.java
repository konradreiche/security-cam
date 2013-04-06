package berlin.reiche.securitas.activities;

import android.util.SparseArray;

/**
 * Actions that are processed by the activities to update the interface.
 * 
 * @author Konrad Reiche
 * 
 */
public enum Action {

	/**
	 * Locks the interface in order to prevent further user input.
	 */
	LOCK_INTERFACE(1),

	/**
	 * Unlocks the interface in order to allow user input again.
	 */
	UNLOCK_INTERFACE(2),

	/**
	 * Turns the interface state to detection mode.
	 */
	SET_DETECTION_MODE(3),

	/**
	 * Turns the interface state to idle mode.
	 */
	SET_IDLE_MODE(4),

	/**
	 * Sets the registration state.
	 */
	SET_REGISTERED_ON_SERVER(5),

	/**
	 * Creates a notification dialog to inform about a problem.
	 */
	REPORT_ERROR(6),

	/**
	 * Updates the snapshot image.
	 */
	SET_SNAPSHOT(7);

	/**
	 * The code is used to translate the mnemonic enumeration constants into an
	 * integer which is used by the Android API.
	 */
	public final int code;

	/**
	 * Default constructor.
	 * 
	 * @param code
	 *            The code is used to translate the mnemonic enumeration
	 *            constants into an integer which is used by the Android API.
	 */
	Action(int code) {
		this.code = code;
	}

	/**
	 * Stores the different code integer values and maps them to their respect
	 * {@link Action} enumeration constant. A sparse array is used, which maps
	 * integers to objects and is more efficient in the implementation.
	 */
	private static final SparseArray<Action> codes;

	static {
		Action[] values = Action.values();
		codes = new SparseArray<Action>(values.length);
		for (Action value : values) {
			codes.put(value.code, value);
		}
	}

	/**
	 * @param what
	 *            the message code.
	 * @return the equivalent enumeration constant.
	 */
	public static Action valueOf(int what) {
		Action value = codes.get(what);
		if (value != null) {
			return codes.get(what);
		}
		throw new IllegalArgumentException();
	}

}
