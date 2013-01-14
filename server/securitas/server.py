from bottle import abort, get, post, request, run, static_file, parse_auth
from events import SnapshotEventHandler
from notifier import AlertNotifier
from watchdog.observers import Observer
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
    """Encapsulates the motion process handling"""

    def __init__(self, settings):
        self.process = None
        self.device = None
        self.control_port = settings['control_port']
        self.settings = settings
        self.snapshot_event = threading.Event()
        self.latest_snapshot = None
        self.notifier = None

    def start(self):
        if self.device is None:
            abort(409, 'Cannot start motion detection without device')
        elif self.process is None:
            LOG.info('Start motion process')
            self.process = subprocess.Popen(['motion', '-c',
                                             'etc/motion.conf'])
        else:
            LOG.info('Motion process already running')

    def stop(self):
        if self.process is not None:
            self.process.kill()
            self.process = None

    def status(self):
        if self.device is None:
            return 'Idle'
        elif self.process is None:
            return 'Ready'
        else:
            return 'Running'

    def set_device(self, id):
        self.device = id
        if id is None:
            self.notifier = None
        else:
            self.notifier = AlertNotifier(self.settings,  id)

    def alert(self, filename):
        self.notifier.notify(filename)

    def request_snapshot(self):
        if self.process is not None:
            url = 'http://localhost:%d/0/action/snapshot' % self.control_port
            requests.get(url)
            self.snapshot_event.wait()
            return self.latest_snapshot

    def notify_about_snapshot(self, filename):
        self.latest_snapshot = filename
        self.snapshot_event.set()
        self.snapshot_event.clear()


settings = util.read_settings('etc/settings.cfg')


def authenticate(func):
    def validate(*args, **kwargs):
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
observer.schedule(event_handler, 'captures', recursive=False)
observer.start()


@get('/server/status', apply=[authenticate])
def get_status():
    return motion.status()


@get('/motion/detection/start', apply=[authenticate])
def start_motion_detection():
    """Starts the motion process including detection for motion"""
    motion.start()
    time.sleep(3)  # camera initialization phase


@get('/motion/detection/stop', apply=[authenticate])
def stop_motion_detection():
    """Stops the motion process"""
    motion.stop()


@get('/server/action/snapshot', apply=[authenticate])
def make_snapshot():
    LOG.debug('Request a current snapshot')
    filename = motion.request_snapshot()
    LOG.debug('Filename is %s' % filename)
    return static_file(filename, root='captures',
                       mimetype='image/jpg')


@get('/static/captures/<filename:re:.*\.jpg>', apply=[authenticate])
def send_snapshot(filename):
    return static_file(filename, root='captures', mimetype='image/jpg')


@post('/device/register', apply=[authenticate])
def register_device():
    id = request.forms.get('id')
    if id is None:
        abort(400, 'Bad request')
    else:
        LOG.debug('Register device %s' % id)
        motion.set_device(id)


@post('/device/unregister', apply=[authenticate])
def unregister_device():
    id = request.forms.get('id')
    LOG.debug('Unregister device %s' % id)
    motion.set_device(None)


if __name__ == '__main__':

    try:
        run(server='paste', host='0.0.0.0', port=4000)
    finally:
        observer.stop()
        sys.exit()
