package berlin.reiche.securitas.controller.tasks;

import static berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand.START;
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
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.DetectionState;
import berlin.reiche.securitas.controller.IdleState;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.HttpUtilities;

public class DetectionRequest extends AsyncTask<String, Void, HttpResponse> {

	public enum DetectionCommand {
		START, STOP
	};

	IOException exception;

	DetectionCommand command;

	ClientModel model;

	ClientController controller;

	private static String TAG = DetectionRequest.class.getSimpleName();

	public DetectionRequest(Model<State> model, ClientController controller,
			DetectionCommand command) {
		this.model = (ClientModel) model;
		this.controller = controller;
		this.command = command;
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
			controller.alertProblem(exception.getMessage());
			controller.setState(new IdleState(controller));
		} else if (response == null) {
			String error = "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.";
			controller.alertProblem(error);
			Log.e(TAG, error);
		} else {
			processResponse(response);
		}
	}

	private void processResponse(HttpResponse response) {
		
		switch (response.getStatusLine().getStatusCode()) {
		case SC_OK:
			performDetectionRequest();
			break;
		case SC_UNAUTHORIZED:
			alertAuthorizationProblem();
			break;
		case SC_CONFLICT:
			handleAbsentDeviceRegistration();
			break;
		default:
			handleOtherErrors(response);
		}
	}

	private void handleOtherErrors(HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		String status = statusLine.getStatusCode() + " ";
		status += statusLine.getReasonPhrase();
		controller.alertProblem(status);
	}

	private void handleAbsentDeviceRegistration() {
		model.setRegisteredOnServer(false);
		controller.setRegisteredOnServer(false);
		controller.alertProblem("Device is not registered yet");
	}

	private void alertAuthorizationProblem() {
		controller.alertProblem("Unauthorized request, check "
				+ "authentication data");
	}

	private void performDetectionRequest() {
		State newState = (command == START) ? State.DETECTING : State.IDLE;
		model.setState(newState);
		boolean detecting = newState == State.DETECTING;
		if (detecting) {
			controller.setState(new DetectionState(controller));
			controller.setDetectionMode();
			controller.downloadLatestSnapshot();
		} else {
			controller.setIdleMode();
		}
	}

}