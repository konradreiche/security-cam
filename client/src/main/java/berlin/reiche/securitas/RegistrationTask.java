package berlin.reiche.securitas;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.os.AsyncTask;

/**
 * The registration task manages the registration ID which is required to
 * receive GCM messages. The registration ID is send to the server application.
 * 
 * It is executed as an {@link AsyncTask} so the UI does not freeze if there is
 * delay.
 * 
 * @author Konrad Reiche
 * 
 */
public class RegistrationTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String registrationId;

    public RegistrationTask(Context context, String registrationId) {
        this.context = context;
        this.registrationId = registrationId;
    }

    @Override
    protected Void doInBackground(Void... params) {

        boolean isRegistered = ServerUtilities.registerDevice(context,
                registrationId);

        if (!isRegistered) {
            GCMRegistrar.unregister(context);
        }

        return null;
    }

}
