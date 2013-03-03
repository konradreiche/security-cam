package berlin.reiche.securitas;

import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.util.Settings;

/**
 * This class is the entry point for the application, consisting of the model,
 * controller and settings for accessing the backend. It also instantiated all
 * the components.
 * 
 * @author Konrad Reiche
 * 
 */
public class Client {

	private static ClientModel model;

	private static ClientController controller;

	public static String endpoint;

	public static Settings settings;

	static {
		model = new ClientModel();
		controller = new ClientController(model);
	}

	public static Settings getSettings() {
		return settings;
	}

	public static ClientController getController() {
		return controller;
	}

}
