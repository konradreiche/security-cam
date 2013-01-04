package berlin.reiche.securitas.util;

public class Settings {

	String host;

	int port;

	String username;

	String password;

	public Settings(String host, String port, String username, String password) {
		super();
		this.host = host;
		this.port = Integer.valueOf(port);
		this.username = username;
		this.password = password;
	}

}
