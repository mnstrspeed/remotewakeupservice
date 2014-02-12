package nl.tomsanders.wakeupservice;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startBackgroundService();
		
		// Give them gay boiz some feedback
		Toast.makeText(getApplicationContext(), "Started service", 
				Toast.LENGTH_SHORT).show();
		
		finish();
	}

	private void startBackgroundService()
	{
		Intent intent = new Intent(this, StatusCheckReceiver.class);
		intent.putExtra("stop", false);
		intent.putExtra("startTime", System.currentTimeMillis() / 1000L);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 
				1000 * 60 * 5, pendingIntent);
	}

}
