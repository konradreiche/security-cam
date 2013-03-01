package berlin.reiche.securitas.tasks;

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
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.util.FlushedInputStream;
import berlin.reiche.securitas.util.HttpUtilities;

public class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {

	IOException exception;

	ClientModel model;

	public BitmapDownloadTask(Model<ClientModel.State> model) {
		super();
		this.model = (ClientModel)model;
	}

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

	@Override
	protected void onPostExecute(Bitmap result) {
		model.setSnapshot(result);
	}

}
