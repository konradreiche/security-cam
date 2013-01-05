from bottle import abort, get, post, request, run, static_file, parse_auth
from gcm import GCM
import datetime
import logging
import subprocess
import requests
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
        self.gcm = GCM(settings['gcm_api_key'])
        self.control_port = settings['control_port']
        self.snapshot_ready = threading.Event()
        self.latest_snapshot = None

    def start(self):
        if self.device is None:
            abort(409, 'Cannot start motion detection without device')
        elif self.process is None:
            LOG.info('Start motion process')
            self.process = subprocess.Popen(['motion','-c','etc/motion.conf'])
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

    def alert(self):
        timestamp = datetime.datetime.now()
        timestamp = timestamp.strftime('%A, %d. %B %Y at %I:%M%p')
        data = {'timestamp': timestamp}
        try:
            self.gcm.plaintext_request(registration_id=self.device, data=data)
            time.slee(5)
        except Exception as e:
            LOG.error(e)

    def set_device(self, id):
        self.device = id

    def request_snapshot(self):
        if self.process is not None:
            url = 'http://localhost:%d/0/action/snapshot' % self.control_port
            requests.get(url)
            self.snapshot_ready.wait()
            return self.latest_snapshot


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


@get('/alerts/motion', apply=[authenticate])
def notify_device_about_motion():
    LOG.debug('Notify device about motion')
    motion.alert()


@get('/server/action/snapshot', apply=[authenticate])
def make_snapshot():
    LOG.debug('Request a current snapshot')
    filename = motion.request_snapshot()
    LOG.debug('Filename is %s' % filename)
    return static_file(filename, root='captures',
                       mimetype='image/jpg')


@post('/server/action/snapshot/ready', apply=[authenticate])
def notify_server_about_snapshot():
    motion.latest_snapshot = request.forms.get('filename')
    motion.snapshot_ready.set()
    motion.snapshot_ready.clear()


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
    run(server='paste', host='0.0.0.0', port=4000)
