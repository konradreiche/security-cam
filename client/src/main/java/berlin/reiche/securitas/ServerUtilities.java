package berlin.reiche.securitas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

/**
 * A collection of methods to control the server application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ServerUtilities {

    private static final String TAG = "ServerUtilities";

    /**
     * Tries to register the device on the server application.
     * 
     * @param context
     *            the context which invoked the method.
     * @param registrationId
     *            the registration ID of the device
     * @return whether the registration was successful
     */
    public static boolean registerDevice(Context context, String registrationId) {

        String url = getEndpoint(context);
        if (url == null) {
            Toast.makeText(context, "There is no server defined yet.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        try {
            executePostRequest(url + "/control/register", parameter);
            GCMRegistrar.setRegisteredOnServer(context, true);
            return true;
        } catch (IOException e) {
            GCMRegistrar.setRegisteredOnServer(context, false);
            Log.e(TAG, "Exception executing POST request:" + e);
            return false;
        }

    }

    public static void unregisterDevice(Context context, String registrationId) {
        unregisterDevice(context, registrationId, null, null);
    }

    public static void unregisterDevice(Context context, String registrationId,
            String host, String port) {

        String url = getEndpoint(context, host, port);
        Log.i(TAG, "Unregistering device");
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        GCMRegistrar.setRegisteredOnServer(context, false);
        try {
            executePostRequest(url + "/control/unregister", parameter);
        } catch (IOException e) {
            Log.e(TAG, "Exception executing POST request:" + e);
        }
    }

    static String getEndpoint(Context context) {
        return getEndpoint(context, null, null);
    }

    static String getEndpoint(Context context, String host, String port) {

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (host == null) {
            host = preferences.getString(SettingsActivity.HOST, "");
        }

        if (port == null) {
            port = preferences.getString(SettingsActivity.PORT, "");
        }

        if (host.equals("")) {
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

        List<NameValuePair> data = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : parameters.entrySet()) {
            data.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(endpoint);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new UrlEncodedFormEntity(data));
        HttpResponse responsePost = client.execute(post);
        HttpEntity resEntityPost = responsePost.getEntity();
        if (resEntityPost == null) {
            throw new IOException("Status Code"
                    + responsePost.getStatusLine().getStatusCode());
        }
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

    public static void startDetection(Context context) {
        String url = getEndpoint(context);
        try {
            executeGetRequest(url + "/control/start",
                    new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopDetection(Context context) {
        String url = getEndpoint(context);
        try {
            executeGetRequest(url + "/control/stop",
                    new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void requestSnapshot(Context context) {
        String url = getEndpoint(context);
        try {
            executeGetRequest(url + "/control/snapshot",
                    new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
