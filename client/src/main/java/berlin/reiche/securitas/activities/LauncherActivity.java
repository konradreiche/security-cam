package berlin.reiche.securitas.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import berlin.reiche.securitas.R;

/**
 * A launcher activity is used to display initial instructions for the user.
 * This is done in order to avoid that users downloading the application from
 * the Android Play Market, using it without reading the description and write a
 * negative review out of frustration.
 * 
 * @author Konrad Reiche
 * 
 */
public class LauncherActivity extends Activity {

	/**
	 * Checks whether the instructions have already been read (user pressed
	 * "Continue") and based on that either creates the construction dialog or
	 * continues to the {@link MainActivity}.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (sp.getBoolean("skip", false)) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		} else {
			createInstructionDialog(sp).show();
		}
	}

	/**
	 * Builds the instruction dialog with two buttons by setting the values and
	 * adding the behavior code for both buttons.
	 * 
	 * @param sp
	 *            the shared preferences as used by this application.
	 * @return a dialog displaying instructions for the first use.
	 */
	public AlertDialog createInstructionDialog(final SharedPreferences sp) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.instructions_title);
		builder.setMessage(R.string.instructions);

		builder.setPositiveButton("Continue", new OnContinueListener(this, sp));
		builder.setNegativeButton("Exit", new OnExitListener());
		return builder.create();
	}

	/**
	 * {@link OnClickListener} for the continue button.
	 * 
	 * @author Konrad Reiche
	 * 
	 */
	private class OnContinueListener implements DialogInterface.OnClickListener {

		/**
		 * The application context, required in order to issue an intent to the
		 * {@link MainActivity}.
		 */
		Context context;

		/**
		 * The shared preferences as used by this application, required in order
		 * to set a skip flag which is used to ensure that the instructions will
		 * always be skipped after pressing continue.
		 */
		SharedPreferences sp;

		/**
		 * Default constructor.
		 * 
		 * @param context
		 *            The application context, required in order to issue an
		 *            intent to the {@link MainActivity}.
		 * @param sp
		 *            The shared preferences as used by this application,
		 *            required in order to set a skip flag which is used to
		 *            ensure that the instructions will always be skipped after
		 *            pressing continue.
		 */
		public OnContinueListener(Context context, SharedPreferences sp) {
			super();
			this.context = context;
			this.sp = sp;
		}

		/**
		 * Sets the skip flag so that the launcher activity will skip the
		 * instruction dialog and continues directly to the {@link MainActivity}
		 * .
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Editor editor = sp.edit();
			editor.putBoolean("skip", true);
			editor.commit();

			Intent i = new Intent(context, MainActivity.class);
			context.startActivity(i);
			finish();
		}
	}

	/**
	 * {@link OnClickListener} for the exit button.
	 * 
	 * @author Konrad Reiche
	 * 
	 */
	private class OnExitListener implements DialogInterface.OnClickListener {

		/**
		 * Shuts down the application.
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	}

}
