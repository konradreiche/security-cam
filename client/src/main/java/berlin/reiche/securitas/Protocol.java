package berlin.reiche.securitas;

import android.util.SparseArray;

public enum Protocol {

	REGISTER_DEVICE(100, "/device/register"),

	UNREGISTER_DEVICE(101, "/device/unregister"),

	START_DETECTION(102, "/motion/detection/start"),

	STOP_DETECTION(103, "/motion/detection/stop"),

	DOWNLOAD_MOTION_SNAPSHOT(104, "/static/captures/"),

	DOWNLOAD_LATEST_SNAPSHOT(105, "/server/action/snapshot"),

	RESTORE_CLIENT_STATE(106, "/server/status");
	

	public final int code;

	public final String operation;

	Protocol(int code, String operation) {
		this.code = code;
		this.operation = operation;
	}

	/**
	 * Stores the different code integer values and maps them to their respect
	 * {@link Protocol} enumeration constant. A sparse array is used, which maps
	 * integers to objects and is more efficient in the implementation.
	 */
	private static final SparseArray<Protocol> codes;

	static {
		Protocol[] values = Protocol.values();
		codes = new SparseArray<Protocol>(values.length);
		for (Protocol value : values) {
			codes.put(value.code, value);
		}
	}

	public static Protocol valueOf(int what) {
		Protocol value = codes.get(what);
		if (value != null) {
			return codes.get(what);
		}
		throw new IllegalArgumentException();
	}
}
