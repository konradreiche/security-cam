package berlin.reiche.securitas;

import android.util.SparseArray;

public enum Protocol {

	START(0),

	STOP(1);

	private final int code;
	
	Protocol(int code) {
		this.code = code;
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
