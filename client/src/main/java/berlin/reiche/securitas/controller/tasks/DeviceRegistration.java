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
import berlin.reiche.securitas.activies.MainActivity;
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

	public enum Command {
		REGISTER, UNREGISTER
	};

	String id;

	Command command;

	ClientModel model;

	ClientController controller;

	IOException exception;

	private static String TAG = MainActivity.class.getSimpleName();

	public DeviceRegistration(Model<State> model, ClientController controller,
			String id, Command command) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
		this.id = id;
		this.command = command;
	}

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

	@Override
	protected void onPostExecute(HttpResponse response) {

		if (exception != null) {
			model.setRegisteredOnServer(false);
			controller.setRegisteredOnServer(false);
		}

		if (response == null) {
			controller.alertProblem("Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else if (response.getStatusLine().getStatusCode() == SC_OK) {
			boolean isRegistered = command == REGISTER;
			model.setRegisteredOnServer(isRegistered);
			controller.setRegisteredOnServer(isRegistered);
		} else {
			Log.i(TAG, response.getStatusLine().getReasonPhrase());
			controller.alertProblem(response.getStatusLine().getReasonPhrase());
		}
	}

}
