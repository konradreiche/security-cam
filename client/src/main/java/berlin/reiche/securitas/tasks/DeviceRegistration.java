package berlin.reiche.securitas.tasks;

import static berlin.reiche.securitas.tasks.DeviceRegistration.DeviceCommand.REGISTER;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.MainActivity;
import berlin.reiche.securitas.util.HttpUtilities;

import com.google.android.gcm.GCMRegistrar;

/**
 * This asynchronous task manages the registration ID which is required to
 * receive GCM messages. The registration ID is send to the server application.
 * 
 * @author Konrad Reiche
 * 
 */
public class DeviceRegistration extends AsyncTask<String, Void, HttpResponse> {

	public enum DeviceCommand {
		REGISTER, UNREGISTER
	};

	Context context;

	String id;

	DeviceCommand command;

	private static String TAG = MainActivity.class.getSimpleName();

	public DeviceRegistration(String id, DeviceCommand command, Context context) {
		super();
		this.id = id;
		this.command = command;
		this.context = context;
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
			GCMRegistrar.setRegisteredOnServer(context, false);
		} finally {
			HttpUtilities.closeClient(client);
		}

		return null;
	}

	@Override
	protected void onPostExecute(HttpResponse response) {

		if (response == null) {
			Log.e(TAG, "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else {
			switch (response.getStatusLine().getStatusCode()) {
			case SC_OK:
				boolean registered = command == REGISTER;
				GCMRegistrar.setRegisteredOnServer(context, registered);
				break;
			default:
				Log.i(TAG, response.getStatusLine().getReasonPhrase());
			}
		}
	}

}
