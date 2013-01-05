package berlin.reiche.securitas.tasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.MainActivity;
import berlin.reiche.securitas.util.FlushedInputStream;
import berlin.reiche.securitas.util.HttpUtilities;

public class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {

	private static final String TAG = BitmapDownloadTask.class.getSimpleName();

	IOException exception;

	ImageView imageView;

	Activity activity;

	public BitmapDownloadTask(Activity activity, ImageView imageView) {
		super();
		this.activity = activity;
		this.imageView = imageView;
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
			Log.e(TAG, e.getMessage());
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		MainActivity activity = (MainActivity) this.activity;
		imageView.setImageBitmap(result);
		activity.unlockUI();
	}

}
