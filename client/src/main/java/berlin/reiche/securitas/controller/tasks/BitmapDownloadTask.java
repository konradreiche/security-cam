package berlin.reiche.securitas.controller.tasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.FlushedInputStream;
import berlin.reiche.securitas.util.HttpUtilities;

/**
 * Asynchronous task for downloading snapshots from the backend.
 * 
 * @author Konrad Reiche
 * 
 */
public class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {

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
	 * The exception is stored in a field, this way the exception can be
	 * processed after the background task has been finished.
	 */
	IOException exception;

	/**
	 * Default constructor.
	 * 
	 * @param model
	 *            the model is used for setting the bitmap after the snapshot
	 *            has been received.
	 * @param controller
	 *            the controller is used to communicate with the interface if
	 *            there are changes.
	 */
	public BitmapDownloadTask(Model<State> model, ClientController controller) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
	}

	/**
	 * Issues a HTTP GET request and reads the response which is used to
	 * construct the final {@link Bitmap}. A customized flushed input stream is
	 * used in order to avoid a bug in the Android library.
	 */
	@Override
	protected Bitmap doInBackground(String... url) {

		HttpClient client = HttpUtilities.newHttpClient();
		HttpGet get = new HttpGet(url[0]);
		HttpUtilities.setAuthorization(get, Client.getSettings());

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			inputStream = new FlushedInputStream(inputStream);
			return BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			exception = e;
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	/**
	 * Sets the snapshot and reports back to interface.
	 */
	@Override
	protected void onPostExecute(Bitmap result) {
		if (exception != null) {
			controller.reportError(exception.getMessage());
		}
		model.setSnapshot(result);
		controller.setSnapshot(result);
	}

}
