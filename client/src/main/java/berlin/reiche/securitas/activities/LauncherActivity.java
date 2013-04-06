package berlin.reiche.securitas.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import berlin.reiche.securitas.R;

public class LauncherActivity extends Activity {

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

	public AlertDialog createInstructionDialog(final SharedPreferences sp) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Attention");
		builder.setMessage("This application will not work without the "
				+ "backend software. Make sure it is installed and running. "
				+ "In the next step you will be asked to configure the endpoint.");

		builder.setPositiveButton("Continue", new OnContinueListener(this, sp));
		builder.setNegativeButton("Exit", new OnExitListener());
		return builder.create();
	}

	private class OnContinueListener implements DialogInterface.OnClickListener {

		Context context;
		SharedPreferences sp;

		public OnContinueListener(Context context, SharedPreferences sp) {
			super();
			this.context = context;
			this.sp = sp;
		}

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

	private class OnExitListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	}

}
