package berlin.reiche.securitas.model;

import android.os.Message;
import android.util.SparseArray;

/**
 * Protocol defining the REST API on which the client-server communication
 * functions.
 * 
 * @author Konrad Reiche
 * 
 */
public enum Protocol {

	/**
	 * Register the device on the endpoint.
	 */
	REGISTER_DEVICE(100, "/device/register"),

	/**
	 * Unregisters the device from the endpoint.
	 */
	UNREGISTER_DEVICE(101, "/device/unregister"),

	/**
	 * Switch the server into the motion detection state.
	 */
	START_DETECTION(102, "/motion/detection/start"),

	/**
	 * Switch the server into the idle state.
	 */
	STOP_DETECTION(103, "/motion/detection/stop"),

	/**
	 * Download a specific motion snapshot.
	 */
	DOWNLOAD_MOTION_SNAPSHOT(104, "/static/captures/"),

	/**
	 * Issues the server to create a current snapshot and downloads this
	 * snapshot afterwards.
	 */
	DOWNLOAD_LATEST_SNAPSHOT(105, "/server/action/snapshot"),

	/**
	 * Synchronize the client state with the server state.
	 */
	RESTORE_CLIENT_STATE(106, "/server/status");

	/**
	 * The integer code for representing the REST action. This is necessary in
	 * order to be compatible with the {@link Message} methods.
	 */
	public final int code;

	/**
	 * The operation which defines a URI path.
	 */
	public final String operation;

	/**
	 * Default constructor.
	 * 
	 * @param code
	 *            the integer representation of the REST URI.
	 * @param operation
	 *            the REST URI which identifies the action.
	 */
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

	/**
	 * Maps an integer code to the respective protocol action. This method is
	 * used when processing messages. This way a {@link Protocol} instance can
	 * be constructed and easily compared.
	 * 
	 * @param what
	 *            the integer code.
	 * @return the protocol instance.
	 */
	public static Protocol valueOf(int what) {
		Protocol value = codes.get(what);
		if (value != null) {
			return codes.get(what);
		}
		throw new IllegalArgumentException();
	}
}
