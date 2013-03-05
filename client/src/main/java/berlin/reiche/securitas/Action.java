package berlin.reiche.securitas;

import android.util.SparseArray;

/**
 * Actions that are processed by the activities to update the interface.
 * 
 * @author Konrad Reiche
 * 
 */
public enum Action {

	LOCK_INTERFACE(1),

	UNLOCK_INTERFACE(2),

	SET_DETECTION_ACTIVE(3),

	SET_DETECTION_INACTICE(4),

	SET_REGISTERED_ON_SERVER(5),
	
	SET_STATUS_TEXT(6),
	
	SET_SNAPSHOT(7);

	public final int code;

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

	public static Action valueOf(int what) {
		Action value = codes.get(what);
		if (value != null) {
			return codes.get(what);
		}
		throw new IllegalArgumentException();
	}

}
