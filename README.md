# Security Cam

Security Cam is a **Python** and **Android** attachment for [Motion][1]. Motion is a program that monitors the video signal from cameras. The Security Cam application is a client-server architecture on top of Motion.

The Python server manages the Motion process and implements a **REST API** for controlling the server. The light-weight client runs on Android which is used to actively control the server.

<img src="http://konrad-reiche.com/images/security-cam/screenshots/device-1.png">
<img src="http://konrad-reiche.com/images/security-cam/screenshots/device-2.png">
<img src="http://konrad-reiche.com/images/security-cam/screenshots/device-3.png">
<img src="http://konrad-reiche.com/images/security-cam/screenshots/device-4.png">
<img src="http://konrad-reiche.com/images/security-cam/screenshots/device-5.png">

## Features

* Start and stop motion detection remotely
* Display current camera snapshots
* Push notifications when motion is detected
* Retrieve camera snapshots that triggered motion
* Upload each snapshot directly to your Dropbox

## Installation

1. Install Motion, either from [package][2] or from [source][3] (recommended).

2. Download the [Android client][6] from the Google Play Market.
   
3. Get the server Python package:
```
$ pip install security-cam
```

4. Get a GCM API Key (required for receiving push notifications)
   - Go to [GCM: Creating a Google API Project][4] and follow the instructions
   - Take note of the API key

5. Get a Dropbox API access (optional)
   - Go to the [Dropbox Apps Console][5] and create a new app
   - App type: Core API
   - Permission: App folder
   - Take note of the API key and App secret

6. Configure the server `/usr/local/etc/security-cam/`:
   - Rename `motion.conf.example -> motion.conf`
   - Rename `settings.cfg.example -> settings.cfg`
   - Edit the `settings.cfg` and configure it to your needs

7. Start the server
   ```
   $ ln -s /usr/local/lib/python/dist-packages/securitas/server.py /usr/bin/security-cam
   $ sudo chmod +x /usr/bin/security-cam
   $ security-cam
   ```

   You will be prompted to authorize the application with your Dropbox account if you have followed step 5.

8. Launch the Android application
   - Configure the settings to your needs
   - Port: 4000

9. Enjoy!

## Future Work

Some ideas for the future development:
- Use Secure Socket Layer (SSL)
- Implement a snapshot picker

[1]: http://www.lavrsen.dk/foswiki/bin/view/Motion/WebHome
[2]: http://www.lavrsen.dk/foswiki/bin/view/Motion/DownloadFiles
[3]: https://github.com/sackmotion/motion
[4]: http://developer.android.com/google/gcm/gs.html#create-proj
[5]: https://www.dropbox.com/developers/apps/
[6]: https://play.google.com/store/apps/details?id=berlin.reiche.securitas
