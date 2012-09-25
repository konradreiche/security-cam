package berlin.reiche.securitas;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

    private static final String TAG = "security-cam";

    public static final String SENDER_ID = "958926895848";

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     *            If the activity is being re-initialized after previously being
     *            shut down then this Bundle contains the data it most recently
     *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
     *            is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);
        register();
    }

    public void startDetection(View view) {
        ServerUtilities.startDetection(this);
    }

    public void stopDetection(View view) {
        ServerUtilities.stopDetection(this);
    }

    private void register() {
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                Log.i(TAG, "Already registered");
            }
            ServerUtilities.registerDevice(this, regId);
        }
    }
    
    public void refresh(View view) {
        ServerUtilities.requestSnapshot(this);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setInitialScale(1);
        String endpoint = ServerUtilities.getEndpoint(this);
        String url = endpoint + "/picture/lastsnap.jpg";
        webView.loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        register();

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setInitialScale(1);
        String endpoint = ServerUtilities.getEndpoint(this);
        String url = endpoint + "/picture/";
        
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            
            Object o = intent.getExtras().get("picture");
            
            
            if (o != null) {                    
                String path = (String) o;
                url += path;
                webView.loadUrl(url);
                GCMIntentService.motions = 0;
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
            }
        } else {
            url += "lastsnap.jpg";
            webView.loadUrl(url);
        }
        
        TextView status = (TextView) findViewById(R.id.connectionStatus);
        if (GCMRegistrar.isRegisteredOnServer(this)) {
            status.setText("Connection Status: OK");
        } else {
            status.setText("Connection Status: Error");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.settings:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            break;
        }
        return true;
    }
}
