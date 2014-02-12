Remote Wake-Up Service lets you set up a link that triggers the alarm on your Android phone from anywhere in the world. Includes some pretty useless features like a map that shows the location from which your link was visited (based on MaxMind's GeoIP database) and the browser/platform that was used.

Installation
------------
1. Set up an Apache web server with mod_rewrite
```
    a2enmod rewrite
    vi /etc/apache2/sites-available/default
      :%s/AllowOverride None/AllowOverride All/g
    service apache2 restart
```
2. Import the contents of `www`
3. Change the IP addresses in `android/src/nl/tomsanders/wakeupservice/StatusCheckReceiver.java` (ideally this will be integrated in the GUI some day, but you know how that goes) and compile

Now, whenever someone visits `http://$YOUR_IP/.../$URL` they will be forwarded to $URL and you will be woken up by your alarm clock. Alternatively, you can create a more discrete URL using Google's URL shortening service: `http://$YOUR_IP/g/$ID` will forward to `http://goo.gl/$ID`.
