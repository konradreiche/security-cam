package berlin.reiche.securitas;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Offers methods to communicate with the server.
 * 
 * @author Konrad Reiche
 * 
 */
public class ServerUtilities {

    private static final String TAG = "security-cam";

    public static void registerDevice(Context context, String registrationId) {

        String url = getEndpoint(context);
        if (url == null) {
            Toast.makeText(context, "There is no server defined yet.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Log.i(TAG, "Registering device");
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        try {
            executePostRequest(url + "/register", parameter);
            GCMRegistrar.setRegisteredOnServer(context, true);
        } catch (IOException e) {
            GCMRegistrar.setRegisteredOnServer(context, false);
            Log.e(TAG, "Exception executing POST request:" + e);
        }
    }

    public static void unregisterDevice(Context context, String registrationId) {

        String url = getEndpoint(context);

        Log.i(TAG, "Unregistering device");
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        try {
            executePostRequest(url + "/unregister", parameter);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            Log.e(TAG, "Exception executing POST request:" + e);
        }
    }

    private static String getEndpoint(Context context) {

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String host = preferences.getString(SettingsActivity.HOST, null);
        String port = preferences.getString(SettingsActivity.PORT, null);
        if (host == null || port == null || host.equals("") || port.equals("")) {
            return null;
        }

        if (!host.startsWith("http://")) {
            host = "http://" + host;
        }

        return host + ":" + port;
    }

    /**
     * Executes a HTTP POST request.
     * 
     * @throws IOException
     */
    private static void executePostRequest(String endpoint,
            Map<String, String> parameters) throws IOException {

        URL url = new URL(endpoint);
        String body = constructBody(parameters);
        byte[] bytes = body.getBytes();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded;charset=UTF-8");

        OutputStream writer = connection.getOutputStream();
        writer.write(bytes);
        writer.close();

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new IOException("HTTP Error Code" + status);
        }

        connection.disconnect();
    }

    /**
     * Executes a HTTP GET request.
     * 
     * @throws IOException
     */
    private static void executeGetRequest(String endpoint,
            Map<String, String> parameters) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(endpoint);
        get.setHeader("Content-Type",
                "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse responseGet = client.execute(get);
        HttpEntity resEntityGet = responseGet.getEntity();
        if (resEntityGet == null) {
            throw new IOException("Status Code"
                    + responseGet.getStatusLine().getStatusCode());
        }
    }

    private static String constructBody(Map<String, String> parameters) {

        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> it = parameters.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> parameter = it.next();
            sb.append(parameter.getKey());
            sb.append("=");
            sb.append(parameter.getValue());
            if (it.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public static void startDetection(Context context) {
        String url = getEndpoint(context);
        try {
            executeGetRequest(url + "/start", new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopDetection(Context context) {
        String url = getEndpoint(context);
        try {
            executeGetRequest(url + "/stop", new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
