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
import berlin.reiche.securitas.controller.states.DetectionState;
import berlin.reiche.securitas.controller.states.IdleState;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.HttpUtilities;

/**
 * Asynchronous task for requesting a change to the motion detection. Either to
 * start the motion detection or to stop the motion detection.
 * 
 * @author Konrad Reiche
 * 
 */
public class DetectionRequest extends AsyncTask<String, Void, HttpResponse> {

	/**
	 * Detection command is used to distinguish between which kind of detection
	 * request issued.
	 * 
	 * @author Konrad Reiche
	 * 
	 */
	public enum DetectionCommand {
		START, STOP
	};

	/**
	 * The exception is stored in a field, this way the exception can be
	 * processed after the background task has been finished.
	 */
	IOException exception;

	/**
	 * Detection command is used to distinguish between which kind of detection
	 * request issued.
	 */
	DetectionCommand command;

	/**
	 * The model is used for setting the bitmap after the snapshot has been
	 * received.
	 */
	ClientModel model;

	/**
	 * The controller is used to communicate with the interface if there are
	 * changes.
	 */
	ClientController controller;

	/**
	 * Tag for logging.
	 */
	private static String TAG = DetectionRequest.class.getSimpleName();

	/**
	 * Default constructor.
	 * 
	 * @param model
	 *            the model is used for setting the bitmap after the snapshot
	 *            has been received.
	 * @param controller
	 *            the controller is used to communicate with the interface if
	 *            there are changes.
	 * @param command
	 *            the kind of detection command that should be issued.
	 */
	public DetectionRequest(Model<State> model, ClientController controller,
			DetectionCommand command) {
		this.model = (ClientModel) model;
		this.controller = controller;
		this.command = command;
	}

	/**
	 * Issues a HTTP GET request based on the given URI.
	 */
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

	/**
	 * Processed the HTTP response.
	 */
	protected void onPostExecute(HttpResponse response) {

		if (exception != null && response == null) {
			controller.reportError(exception.getMessage());
			controller.setState(new IdleState(controller));
		} else if (response == null) {
			String error = "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.";
			controller.reportError(error);
			Log.e(TAG, error);
		} else {
			processResponse(response);
		}
	}

	/**
	 * There are three possible outcomes: the server has executed the request,
	 * the request was made with credentials which could not be authenticated
	 * and the server is not ready to receive detection requests.
	 * 
	 * @param response
	 *            the response to the request.
	 */
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

	/**
	 * Processes all other responses.
	 * 
	 * @param response
	 *            the response to the request.
	 */
	private void handleOtherErrors(HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		String status = statusLine.getStatusCode() + " ";
		status += statusLine.getReasonPhrase();
		controller.reportError(status);
	}

	/**
	 * Process HTTP 409 (Conflict)
	 */
	private void handleAbsentDeviceRegistration() {
		model.setRegisteredOnServer(false);
		controller.issueDeviceRegistration();
		controller.reportError("Device is not registered yet");
	}

	/**
	 * Reports the authorization problem to the interface.
	 */
	private void alertAuthorizationProblem() {
		controller.reportError("Unauthorized request, check "
				+ "authentication data");
	}

	/**
	 * Finishes the detection request by updating the model and interface.
	 */
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