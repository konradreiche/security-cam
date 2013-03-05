package berlin.reiche.securitas;

import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.util.Settings;

/**
 * This class is the entry point for the application, consisting of the model,
 * controller and settings for accessing the backend.
 * 
 * @author Konrad Reiche
 * 
 */
public class Client {

	private static ClientModel model;

	private static ClientController controller;

	public static String endpoint;

	public static Settings settings;

	public static Settings getSettings() {
		return settings;
	}

	public static ClientModel getModel() {
		return model;
	}

	public static void setModel(ClientModel model) {
		Client.model = model;
	}

	public static ClientController getController() {
		return controller;
	}

	public static void setController(ClientController controller) {
		Client.controller = controller;
	}

}
