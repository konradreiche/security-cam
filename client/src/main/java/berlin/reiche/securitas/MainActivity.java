package berlin.reiche.securitas;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

/**
 * The main activity is the screen that is shown after the application is
 * started. The main activity deals with the user inputs to control the motion
 * detection and reception of camera pictures.
 * 
 * @author Konrad Reiche
 * 
 */
public class MainActivity extends Activity {

    static final String SENDER_ID = "958926895848";

    String host;

    String port;

    ImageDownloader imageDownloader;

    /**
     * Task responsible for registering the device on the GCM service.
     */
    RegistrationTask registrationTask;

    SharedPreferences pref;

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
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        setContentView(R.layout.main);
        registerDevice();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        host = pref.getString(SettingsActivity.HOST, null);
        port = pref.getString(SettingsActivity.PORT, null);
        imageDownloader = new ImageDownloader();
    }

    public void startDetection(View view) {
        ServerUtilities.startDetection(this);
        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        findViewById(R.id.refresh).setVisibility(View.VISIBLE);
    }

    public void stopDetection(View view) {
        ServerUtilities.stopDetection(this);
        findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
        findViewById(R.id.refresh).setVisibility(View.INVISIBLE);
    }

    /**
     * Registers the device on the GCM service. If the device is already
     * registered the cached registration ID will be used.
     */
    private void registerDevice() {

        final String registrationId = GCMRegistrar.getRegistrationId(this);
        if (registrationId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            if (!GCMRegistrar.isRegisteredOnServer(this)) {
                registrationTask = new RegistrationTask(this, registrationId);
                registrationTask.execute(null, null);
            }
        }
    }

    public void refresh(View view) {

        ServerUtilities.requestSnapshot(this);
        String endpoint = ServerUtilities.getEndpoint(this);
        String url = endpoint + "/picture/lastsnap.jpg";
        
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageDownloader = new ImageDownloader();
        imageDownloader.download(url, imageView);
    }

    @Override
    public void onResume() {

        super.onResume();
        String host = pref.getString(SettingsActivity.HOST, "");
        String port = pref.getString(SettingsActivity.PORT, "");
        if (!host.equals(this.host) || !port.equals(this.port)) {

            if (GCMRegistrar.isRegisteredOnServer(this)) {
                String registrationId = GCMRegistrar.getRegistrationId(this);
                ServerUtilities.unregisterDevice(this, registrationId,
                        this.host, this.port);
            }

            this.host = host;
            this.port = port;
            registerDevice();
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        String endpoint = ServerUtilities.getEndpoint(this);
        String url = endpoint + "/picture/";

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {

            Object o = intent.getExtras().get("picture");

            if (o != null) {
                String path = (String) o;
                url += path;
                findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                findViewById(R.id.refresh).setVisibility(View.VISIBLE);
                imageDownloader.download(url, imageView);
                GCMIntentService.motions = 0;
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                        .cancelAll();
            }
        } else {
            url += "lastsnap.jpg";
            imageDownloader.download(url, imageView);
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

    /**
     * If the activity is finishing or being destroyed by the system it has to
     * made sure that the registration is canceled or undone.
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {

        if (registrationTask != null) {
            registrationTask.cancel(true);
        }
        GCMRegistrar.setRegisteredOnServer(this, false);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }
}
