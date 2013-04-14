package berlin.reiche.securitas;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import berlin.reiche.securitas.activities.MainActivity;
import berlin.reiche.securitas.activities.SettingsActivity;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.model.ClientModel;

/**
 * Locator class for referencing the main components.
 * 
 * @author Konrad Reiche
 * 
 */
public class Client {

	/**
	 * Application model.
	 */
	private static ClientModel model;

	/**
	 * Application controller.
	 */
	private static ClientController controller;

	/**
	 * Application settings.
	 */
	private static Settings settings;

	/**
	 * Endpoint from the backend server in the form
	 * <code>http://host:port</code>
	 */
	private static String endpoint;

	/**
	 * Whether the endpoint configuration is completed. Used to force the user
	 * on the {@link SettingsActivity} otherwise.
	 */
	private static boolean configured;

	/**
	 * @return the application model.
	 */
	public static ClientModel getModel() {
		return model;
	}

	/**
	 * The application model should only be set, respectively created at one
	 * point, that is the creation time of the {@link MainActivity}.
	 * 
	 * @param model
	 *            the application model.
	 */
	public static void setModel(ClientModel model) {
		Client.model = model;
	}

	/**
	 * @return the application controller.
	 */
	public static ClientController getController() {
		return controller;
	}

	/**
	 * The application controller should only be set, respectively created at
	 * one point, that is the creation time of the {@link MainActivity}.
	 * 
	 * @param controller
	 *            the application controller.
	 */
	public static void setController(ClientController controller) {
		Client.controller = controller;
	}

	/**
	 * @return Endpoint from the backend server in the form
	 *         <code>http://host:port</code>
	 */
	public static String getEndpoint() {
		return endpoint;
	}

	/**
	 * This method is private, since it only needs to be called once. That is
	 * when the application settings are updated.
	 * 
	 * @param endpoint
	 *            Endpoint from the backend server in the form
	 *            <code>http://host:port</code>
	 */
	private static void setEndpoint(String endpoint) {
		Client.endpoint = endpoint;
	}

	/**
	 * @return the application settings.
	 */
	public static Settings getSettings() {
		return settings;
	}

	/**
	 * This method should be called when the settings are updated. That should
	 * be the case after the {@link SettingsActivity} was processed.
	 * 
	 * @param context
	 *            the context of the preferences whose values are wanted.
	 * 
	 * @return whether the settings for the backend are complete.
	 */
	public static boolean updateSettings(Context context) {

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);

		String host = sp.getString(SettingsActivity.HOST, "");
		String port = sp.getString(SettingsActivity.PORT, "");
		String username = sp.getString(SettingsActivity.USER, "");
		String password = sp.getString(SettingsActivity.PASSWORD, "");
		String id = sp.getString(SettingsActivity.GCM_SENDER_ID, "");

		configured = !host.equals("") && !port.equals("")
				&& !username.equals("") && !password.equals("")
				&& !id.equals("");

		if (configured) {
			Client.settings = new Settings(host, port, username, password, id);
			setEndpoint("http://" + host + ":" + port);
		}
		return configured;
	}

	/**
	 * Checks whether the endpoint is completely configured.
	 * 
	 * @param context
	 *            the context of the preferences whose values are wanted.
	 * @return whether the endpoint configuration is complete.
	 */
	public static boolean isConfigured(Context context) {
		updateSettings(context);
		return configured;
	}

}
