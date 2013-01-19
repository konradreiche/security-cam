package berlin.reiche.securitas.tasks;

import static berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand.START;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.Model.State;
import berlin.reiche.securitas.util.HttpUtilities;

public class DetectionRequest extends AsyncTask<String, Void, HttpResponse> {

	public enum DetectionCommand {
		START, STOP
	};

	IOException exception;

	DetectionCommand command;

	Model model;

	private static String TAG = DetectionRequest.class.getSimpleName();

	public DetectionRequest(DetectionCommand command, Model model) {
		this.command = command;
		this.model = model;
	}

	@Override
	protected HttpResponse doInBackground(String... uri) {

		HttpClient client = HttpUtilities.newHttpClient();
		HttpGet get = new HttpGet(uri[0]);
		HttpUtilities.setAuthorization(get, Client.getSettings());

		try {
			return client.execute(get);
		} catch (IOException e) {
			exception = e;
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	protected void onPostExecute(HttpResponse response) {

		if (exception != null && response == null) {
			model.setStatus(exception.getMessage());
			model.onRequestFail();
		} else if (response == null) {
			Log.e(TAG, "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else {
			int code = response.getStatusLine().getStatusCode();
			switch (code) {
			case SC_OK:
				State state = (command == START) ? State.DETECTING : State.IDLE;
				model.setState(state);
				break;
			case SC_UNAUTHORIZED:
				model.setStatus("Unauthorized request, check authentication data");
				model.onRequestFail();
				break;
			case SC_CONFLICT:
				model.setRegisteredOnServer(false);
				break;
			default:
				StatusLine statusLine = response.getStatusLine();
				String status = statusLine.getStatusCode() + " ";
				status += statusLine.getReasonPhrase();
				model.setStatus(status);
				model.onRequestFail();
			}
		}
	}

}
