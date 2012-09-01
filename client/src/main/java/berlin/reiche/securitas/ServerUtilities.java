package berlin.reiche.securitas;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.util.Log;

/**
 * Offers methods to communicate with the server.
 * 
 * @author Konrad Reiche
 * 
 */
public class ServerUtilities {

    private static final String TAG = "security-cam";

    private static final String URL = "http://192.168.1.101:3030";

    public static void registerDevice(Context context, String registrationId) {
        Log.i(TAG, "Registering device");

        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        try {
            executePostRequest(URL + "/register", parameter);
            GCMRegistrar.setRegisteredOnServer(context, true);
        } catch (IOException e) {
            Log.e(TAG, "Exception executing POST request:" + e);
        }
    }

    public static void unregisterDevice(Context context, String registrationId) {

        Log.i(TAG, "Unregistering device");
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("registrationId", registrationId);
        try {
            executePostRequest(URL + "/unregister", parameter);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            Log.e(TAG, "Exception executing POST request:" + e);
        }
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
}