package berlin.reiche.securitas;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gcm.GCMRegistrar;

public class HelloAndroidActivity extends Activity {

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
    
    /** Called when the user clicks the Send button */
    public void unregister(View view) {
        GCMRegistrar.unregister(this);
    }
}
