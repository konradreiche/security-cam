package berlin.reiche.securitas.util;

public class Settings {

	private final String host;

	private final int port;

	private final String username;

	private final String password;

	private final String gcmSenderId;

	public Settings(String host, String port, String username, String password,
			String gcmSenderId) {
		super();
		this.host = host;
		this.port = Integer.valueOf(port);
		this.username = username;
		this.password = password;
		this.gcmSenderId = gcmSenderId;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getGcmSenderId() {
		return gcmSenderId;
	}

}
