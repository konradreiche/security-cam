package berlin.reiche.securitas;

/**
 * Application settings which are required in order to establish a connection to
 * the backend server.
 * 
 * @author Konrad Reiche
 * 
 */
public class Settings {

	/**
	 * The host address of the backend server.
	 */
	private final String host;

	/**
	 * The port on which the backend server listens.
	 */
	private final int port;

	/**
	 * The username for authenticating with the backend server.
	 */
	private final String username;

	/**
	 * The password for authenticating with the backend server.
	 */
	private final String password;

	/**
	 * The GCM Sender ID is used in the registration process to identify the
	 * application that is permitted to send messages to the device.
	 */
	private final String gcmSenderId;

	/**
	 * Default constructor.
	 * 
	 * @param host
	 *            the host address of the backend server.
	 * @param port
	 *            the port on which the backend server listens.
	 * @param username
	 *            the username for authenticating with the backend server.
	 * @param password
	 *            the password for authenticating with the backend server.
	 * @param id
	 *            the GCM Sender ID is used in the registration process to
	 *            identify the application that is permitted to send messages to
	 *            the device.
	 */
	public Settings(String host, String port, String username, String password,
			String id) {
		super();
		this.host = host;
		this.port = Integer.valueOf(port);
		this.username = username;
		this.password = password;
		this.gcmSenderId = id;
	}

	/**
	 * @return the host address of the backend server.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port on which the backend server listens.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the username for authenticating with the backend server.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password for authenticating with the backend server.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the GCM Sender ID is used in the registration process to identify
	 *         the application that is permitted to send messages to the device.
	 */
	public String getGcmSenderId() {
		return gcmSenderId;
	}

}
