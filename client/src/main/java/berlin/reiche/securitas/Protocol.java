package berlin.reiche.securitas;

import android.util.SparseArray;

public enum Protocol {

	REGISTER_DEVICE(0, "/device/register"),

	UNREGISTER_DEVICE(1, "/device/unregister"),

	START_DETECTION(2, "/motion/detection/start"),

	STOP_DETECTION(3, "/motion/detection/stop"),

	DOWNLOAD_SNAPSHOT(4, "/static/captures/"),

	DOWNLOAD_LATEST_SNAPSHOT(5, "/server/action/snapshot");

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
