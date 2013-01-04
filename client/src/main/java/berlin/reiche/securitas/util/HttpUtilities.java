package berlin.reiche.securitas.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;

public class HttpUtilities {

	private static final String TAG = HttpUtilities.class.getSimpleName();

	// configuration in one place
	public static AndroidHttpClient newHttpClient() {
		AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		client.getParams().setIntParameter("http.connection.timeout", 2000);
		return client;
	}

	public static void setAuthorization(HttpRequestBase request,
			Settings settings) {

		String user = settings.username;
		String password = settings.password;
		byte[] rawCredentials = (user + ":" + password).getBytes();
		String credentials = Base64.encodeToString(rawCredentials,
				Base64.URL_SAFE | Base64.NO_WRAP);
		request.setHeader("Authorization", "Basic " + credentials);
	}

	public static void setRequestBody(HttpPost request, String[][] data) {

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < data.length; i++) {
			nameValuePairs.add(new BasicNameValuePair(data[i][0], data[i][1]));
		}
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "The encoding is not supported: " + e.getMessage());
		}
	}

	public static void closeClient(HttpClient client) {
		if (client instanceof AndroidHttpClient) {
			((AndroidHttpClient) client).close();
		} else {
			client.getConnectionManager().shutdown();
		}

	}

}
