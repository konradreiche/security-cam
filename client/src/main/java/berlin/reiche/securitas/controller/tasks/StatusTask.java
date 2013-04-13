package berlin.reiche.securitas.controller.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.states.DetectionState;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.HttpUtilities;

/**
 * This task is used to synchronize the client with the state of the server.
 * 
 * @author Konrad Reiche
 * 
 */
public class StatusTask extends AsyncTask<String, Void, HttpResponse> {

	/**
	 * Different states of the server.
	 * 
	 * @author Konrad Reiche
	 * 
	 */
	public enum ServerStatus {
		IDLE, READY, RUNNING
	}

	/**
	 * Tag for logging.
	 */
	private static final String TAG = StatusTask.class.getSimpleName();

	/**
	 * The model is required for updating the state.
	 */
	ClientModel model;

	/**
	 * The controller is required for updating the interface.
	 */
	ClientController controller;

	/**
	 * If the status task is executed due to the selection of a motion
	 * notification this string is used to fetch the snapshot during the
	 * synchronization.
	 */
	String motionFilename;

	/**
	 * The exception is stored here so it can be post-processed after the task
	 * has been executed.
	 */
	IOException exception;

	/**
	 * Default constructor.
	 * 
	 * @param model
	 *            the model is required for updating the state.
	 * @param controller
	 *            the controller is required for updating the interface.
	 * @param motionFilename
	 *            if the status task is executed due to the selection of a
	 *            motion notification this string is used to fetch the snapshot
	 *            during the synchronization.
	 */
	public StatusTask(Model<State> model, ClientController controller,
			String motionFilename) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
		this.motionFilename = motionFilename;
	}

	/**
	 * Performs the HTTP request with the provided URI.
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
	 * Reads the content of the response and processes it.
	 */
	@Override
	protected void onPostExecute(HttpResponse response) {

		if (exception != null) {
			String message = "Get status failed, due to ";
			message += exception.getMessage();
			Log.e(TAG, message);
			controller.reportError(message);
		}

		if (response == null) {
			controller.unlockInterface(false);
			return;
		}

		try {
			String content = readString(response.getEntity().getContent());
			if (content == null) {
				Log.i(TAG, "Status response is null, retry.");
				controller.restoreClientState(motionFilename);
				return;
			}
			content = content.toUpperCase(Locale.US);
			ServerStatus status = ServerStatus.valueOf(content);
			processResponse(response, status);
		} catch (IOException e) {
			String problem = "The stream of the response could not be created.";
			Log.e(TAG, problem);
			controller.unlockInterface(false);
			controller.reportError(problem);
		}
	}

	/**
	 * Processes the response and performs different action based on the server
	 * state.
	 * 
	 * @param response
	 *            the response to the request.
	 * @param status
	 *            the parsed server state.
	 */
	private void processResponse(HttpResponse response, ServerStatus status) {

		switch (status) {
		case IDLE:
			model.setRegisteredOnServer(false);
			controller.issueDeviceRegistration();
			controller.unlockInterface(false);
			break;
		case READY:
			controller.unlockInterface(false);
			break;
		case RUNNING:
			model.setState(State.DETECTING);
			controller.setState(new DetectionState(controller));
			controller.setDetectionMode();
			if (motionFilename == null) {
				controller.downloadLatestSnapshot();
			} else {
				controller.downloadMotionSnapshot(motionFilename);
			}
			break;
		}
	}

	/**
	 * Utility method for reading an {@link InputStream} to a string.
	 * 
	 * @param stream
	 *            the stream of the received response.
	 * @return the content read into a string.
	 */
	public static String readString(InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : null;
	}

}
