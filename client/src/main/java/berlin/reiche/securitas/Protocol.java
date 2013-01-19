package berlin.reiche.securitas;

import android.util.SparseArray;

public enum Protocol {

	START_DETECTION(0, "/motion/detection/start"),

	STOP_DETECTION(1, "/motion/detection/stop");

	public final int code;

	public final String operation;

	Protocol(int code, String operation) {
		this.code = code;
		this.operation = operation;
	}

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
