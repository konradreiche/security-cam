package berlin.reiche.securitas.controller.tasks;

import static berlin.reiche.securitas.controller.tasks.DeviceRegistration.Command.REGISTER;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.activities.MainActivity;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.HttpUtilities;

/**
 * This asynchronous task manages the registration ID which is required to
 * receive GCM messages. The registration ID is send to the server application.
 * 
 * @author Konrad Reiche
 * 
 */
public class DeviceRegistration extends AsyncTask<String, Void, HttpResponse> {

	/**
	 * This command is used to distinguish whether the device should be
	 * registered or unregistered with/from the endpoint.
	 * 
	 * @author Konrad Reiche
	 * 
	 */
	public enum Command {
		REGISTER, UNREGISTER
	};

	/**
	 * Used to uniquely identify the device.
	 */
	String id;

	/**
	 * This command is stored for post-processing after the task has finished.
	 */
	Command command;

	/**
	 * The model is required for updating the state.
	 */
	ClientModel model;

	/**
	 * The controller is required for updating the interface.
	 */
	ClientController controller;

	/**
	 * The exception is post-processed after the task has finished.
	 */
	IOException exception;

	/**
	 * Tag for logging.
	 */
	private static String TAG = MainActivity.class.getSimpleName();

	/**
	 * Default constructor.
	 * 
	 * @param model
	 *            the model is required for updating the state.
	 * @param controller
	 *            the controller is required for updating the interface.
	 * @param id
	 *            the id for the device registration.
	 * @param command
	 *            whether the id should be registered or unregistered.
	 */
	public DeviceRegistration(Model<State> model, ClientController controller,
			String id, Command command) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
		this.id = id;
		this.command = command;
	}

	/**
	 * Performs the HTTP request with the provided URI.
	 */
	@Override
	protected HttpResponse doInBackground(String... uri) {

		Log.i(TAG, "Perform " + command + " POST request on endpoint.");
		HttpClient client = HttpUtilities.newHttpClient();
		String[][] data = { { "id", id } };
		HttpPost post = new HttpPost(uri[0]);
		HttpUtilities.setAuthorization(post, Client.getSettings());
		HttpUtilities.setRequestBody(post, data);

		try {
			return client.execute(post);
		} catch (IOException e) {
			Log.i(TAG, command + " failed, due to " + e.getMessage());
			exception = e;
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	/**
	 * Update the model and interface afterwards.
	 */
	@Override
	protected void onPostExecute(HttpResponse response) {

		if (exception != null) {
			model.setRegisteredOnServer(false);
			controller.setRegisteredOnServer(false);
		}

		if (response == null) {
			controller.reportError("Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else if (response.getStatusLine().getStatusCode() == SC_OK) {
			boolean isRegistered = command == REGISTER;
			model.setRegisteredOnServer(isRegistered);
			controller.setRegisteredOnServer(isRegistered);
		} else {
			Log.i(TAG, response.getStatusLine().getReasonPhrase());
			controller.reportError(response.getStatusLine().getReasonPhrase());
		}
	}

}
