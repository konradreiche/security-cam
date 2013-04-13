#!/usr/bin/env python
"""
HTTP server for controlling the Motion process and serving the Android
application. The module consists of the motion process and the functions for
routing the HTTP requests.
"""
from bottle import abort, get, post, request, run, static_file, parse_auth
from events import SnapshotEventHandler
from notifier import AlertNotifier
from watchdog.observers import Observer

import os
import logging
import requests
import subprocess
import sys
import threading
import time
import util

LOG = logging.getLogger(__name__)
LOG.setLevel(logging.DEBUG)
LOG.addHandler(logging.StreamHandler())


class MotionProcess(object):
    """
    Encapsulates the motion process handling.
    """

    def __init__(self, settings):
        self.process = None
        self.device = None
        self.control_port = settings['control_port']
        self.settings = settings
        self.snapshot_event = threading.Event()
        self.latest_snapshot = None
        self.notifier = None

    def start(self):
        """
        Encapsulates the start procedure for creating the Motion process.
        """

        if self.device is None:
            abort(409, 'Cannot start motion detection without device')
        elif self.process is None:
            LOG.info('Start motion process')
            default_path = '/usr/local/etc/security-cam/motion.conf'
            if os.path.exists(default_path):
                self.process = subprocess.Popen(['motion', '-c', default_path])
            else:
                self.process = subprocess.Popen(['motion', '-c',
                                                 'conf/motion.conf'])
        else:
            LOG.info('Motion process already running')

    def stop(self):
        """
        Encapsulates the start procedure for killing the Motion process.
        """

        if self.process is not None:
            self.process.kill()
            self.process = None

    def status(self):
        """
        Returns the server state based on the device and process state.
        """

        if self.device is None:
            return 'Idle'
        elif self.process is None:
            return 'Ready'
        else:
            return 'Running'

    def set_device(self, identifier):
        """
        Method for registering the device with the server.
        """

        self.device = identifier
        if identifier is None:
            self.notifier = None
        else:
            self.notifier = AlertNotifier(self.settings,  identifier)

    def alert(self, filename):
        """
        Sends a push notification to the registered device.
        """

        self.notifier.notify(filename)

    def request_snapshot(self):
        """
        Issues the creation of a new snapshot and returns it after the file has
        been saved.
        """

        url = 'http://localhost:%d/0/action/snapshot' % self.control_port
        requests.get(url)
        self.snapshot_event.wait()
        return self.latest_snapshot

    def notify_about_snapshot(self, filename):
        """
        Used to set the filename and clear the lock so the request snapshot
        routine eventually returns the image.
        """

        self.latest_snapshot = filename
        self.snapshot_event.set()
        self.snapshot_event.clear()


default_path = '/usr/local/etc/security-cam/settings.cfg'
if os.path.exists(default_path):
    settings = util.read_settings(default_path)
else:
    settings = util.read_settings('conf/settings.cfg')


def authenticate(func):
    """
    Parses the credentials from the HTTP header and validates them.
    """

    def validate(*args, **kwargs):
        """
        Validation function for checking the credentials.
        """

        auth_header = request.headers.get('Authorization')
        if auth_header is None:
            abort(401, 'Access denied')
        credentials = parse_auth(auth_header)

        if (credentials[0] == settings['user'] and
                credentials[1] == settings['password']):
            return func(**kwargs)
        else:
            abort(401, 'Access denied')

    return validate

motion = MotionProcess(settings)
event_handler = SnapshotEventHandler(motion)
observer = Observer()
observer.schedule(event_handler, settings['captures_path'], recursive=False)
observer.start()


@get('/server/status', apply=[authenticate])
def get_status():
    """
    For synchnorizing the client with the server state.
    """

    return motion.status()


@get('/motion/detection/start', apply=[authenticate])
def start_motion_detection():
    """
    Starts the motion process including detection for motion.
    """

    motion.start()
    time.sleep(3)  # camera initialization phase


@get('/motion/detection/stop', apply=[authenticate])
def stop_motion_detection():
    """
    Stops the motion process by killing it.
    """

    motion.stop()


@get('/server/action/snapshot', apply=[authenticate])
def make_snapshot():
    """
    Issues the creation of a new snapshot.
    """

    filename = motion.request_snapshot()
    return static_file(filename, root='captures',
                       mimetype='image/jpg')


@get('/static/captures/<filename:re:.*\.jpg>', apply=[authenticate])
def send_snapshot(filename):
    """
    Returns the specified snapshot, used for returning either a new current
    snapshot or a snapshot that triggered the motion detection.
    """

    return static_file(filename, root='captures', mimetype='image/jpg')


@post('/device/register', apply=[authenticate])
def register_device():
    """
    Registers the client as device with the server. The identifier has to be
    defined in the HTTP header.
    """

    identifier = request.forms.get('id')
    if identifier:
        LOG.debug('Register device %s' % identifier)
        motion.set_device(identifier)
    else:
        abort(400, 'Bad request')


@post('/device/unregister', apply=[authenticate])
def unregister_device():
    """
    Unregisters the device again.
    """

    identifier = request.forms.get('identifier')
    LOG.debug('Unregister device %s' % identifier)
    motion.set_device(None)


if __name__ == '__main__':

    try:
        run(server='paste', host='0.0.0.0', port=4000)
    except Exception as e:
        LOG.warning(e)
    finally:
        observer.stop()
        sys.exit()
