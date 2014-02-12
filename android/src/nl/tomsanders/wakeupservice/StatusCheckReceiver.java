package nl.tomsanders.wakeupservice;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.AlarmClock;
import android.util.DisplayMetrics;
import android.util.Log;

public class StatusCheckReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Log.d("nl.tomsanders.wakeupservice", "hello there");
		final boolean stop = intent.getBooleanExtra("stop", false);
		final long startTime = intent.getLongExtra("startTime", 0L);
		
		if (stop)
		{
			Log.d("nl.tomsanders.wakeupservice", "aye cap, sinking ship");
			stopRepeating(context);
			cancelNotification(context);
		}
		else
		{
			new Thread(new Runnable() {
				@Override
				public void run()
				{
					checkStatus(context, startTime);
				}
			}).start();
		}
	}
	
	private void cancelNotification(Context context)
	{
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	private void checkStatus(Context context, long startTime)
	{
		String url = "http://188.226.148.202/status.php?since=" + startTime;
		try
		{
			String output = getPageContent(url);
			Log.d("nl.tomsanders.wakeupservice", "Status: " + output);
			
			String ip = "";
			String useragent = "";
			boolean activated = !output.equals("0");
			if (activated)
			{
				String[] fields = output.split("\t");
				ip = fields[1];
				useragent = fields[2];
				
				setAlarm(context, 1);
				setAlarm(context, 6);
				
				stopRepeating(context);
			}
			showNotification(context, activated, ip, useragent);
		} 
		catch (Exception e)
		{
			Log.e("nl.tomsanders.wakeupservice", "woops?");
			e.printStackTrace();
		}
	}

	private String getPageContent(String url) throws IOException,
			ClientProtocolException
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		return EntityUtils.toString(response.getEntity());
	}

	private void setAlarm(Context context, int minutesFromNow)
	{
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, minutesFromNow);
		
		Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
		alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "Your Wake-Up Service");
		alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, now.get(Calendar.HOUR_OF_DAY));
		alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, now.get(Calendar.MINUTE));
		alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
		
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(alarmIntent);
		
		Log.d("nl.tomsanders.wakeupservice", "Setting alarm for " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE));
	}

	private void stopRepeating(Context context)
	{
		Intent intent = new Intent(context, StatusCheckReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	private void showNotification(Context context, boolean activated, String ip, String useragent)
	{
		String status = activated ? "Activity detected" : "Nothing yet";
		
		// Stop intent
		Intent intent = new Intent("ACTION_STOP", 
				Uri.parse("wakeupservice:" + System.currentTimeMillis()));
		intent.setClass(context, StatusCheckReceiver.class);
		intent.putExtra("stop", true);
		PendingIntent stopIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notificationBase = new Notification.Builder(context)
			.setContentTitle("Wake-Up Service")
			.setContentText(status)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(stopIntent);
		
		Notification notification;
		if (activated)
		{
			Bitmap mapImage = getMapImage(context, ip);
			notification = new Notification.BigPictureStyle(notificationBase)
				.setSummaryText("Used from " + getBrowserPlatform(useragent))
				.bigPicture(mapImage).build();
		}
		else
		{
			notification = notificationBase.build();
		}
		
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(0, notification);
	}


	private String getBrowserPlatform(String ua)
	{
		ua = ua.toLowerCase();
		
		String platform = "unknown platform";
		String browser = "unknown browser";
		
		if (ua.contains("windows")) {
			platform = "Windows";
		} else if (ua.contains("linux")) {
			platform = "Linux";
		} else if (ua.contains("android")) {
			if (ua.contains("mobile")) {
				platform = "Android (phone)";
			} else {
				platform = "Android (tablet)";
			}
		} else if (ua.contains("iphone")) {
			platform = "Apple iPhone";
		} else if (ua.contains("ipad")) {
			platform = "Apple iPad";
		} else if (ua.contains("apple")) {
			platform = "Mac OS X";
		}
		
		if (ua.contains("chrome")) {
			browser = "Chrome";
		} else if (ua.contains("msie")) {
			browser = "Internet Explorer";
		} else if (ua.contains("firefox")) {
			browser = "Firefox";
		} else if (ua.contains("apple")) {
			browser = "Safari";
		}
		
		return browser + " on " + platform;
	}

	private Bitmap getMapImage(Context context, String ip)
	{
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	    int imageHeight = Math.round(256 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	    int imageWidth = displayMetrics.widthPixels;
	    
		try
		{
			String location = getPageContent("http://188.226.148.202/iplookup.php?ip=" + ip);
		    Log.d("nl.tomsanders.wakeupservice", "showing " + location);
			
			URL mapURL = new URL("http://maps.google.com/maps/api/staticmap?markers=" + 
					location + "&zoom=6&size=" + imageWidth + "x" + imageHeight + "&sensor=false");
			return BitmapFactory.decodeStream(mapURL.openConnection().getInputStream());
		} 
		catch (Exception e)
		{
			Log.e("nl.tomsanders.wakeupservice", "couldn't load map image");
			e.printStackTrace();
			
			// Return empty bitmap
			return Bitmap.createBitmap(displayMetrics, imageWidth, imageHeight, null);
		}
	}
}
