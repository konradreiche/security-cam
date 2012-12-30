from bottle import abort, get, post, request, run, static_file, parse_auth
from gcm import GCM
import ConfigParser
import fileinput
import logging
import subprocess
import requests

LOG = logging.getLogger(__name__)
LOG.setLevel(logging.DEBUG)
LOG.addHandler(logging.StreamHandler())


class MotionProcess(object):
    """Encapsulates the motion process handling"""

    def __init__(self):
        self.process = None
        self.device = None
        self.gcm = GCM('API_KEY')

        for line in fileinput.input('motion.conf'):
            split = line.split('control_port')
            if len(split) is 2:
                self.control_port = int(split[1])
                LOG.debug('Found control port %d' % self.control_port)
                break

    def start(self):
        if self.process is None and self.device is not None:
            self.process = subprocess.Popen(['motion'])

    def stop(self):
        if self.process is not None:
            self.process.kill()
            self.process = None

    def alert(self):
        data = {}
        self.gcm.plaintext_request(registration_id=self.device, data=data)

    def set_device(self, id):
        self.device = id

    def request_snapshot(self):
        if self.process is not None:
            url = 'http://localhost:%d/0/action/snapshot' % self.control_port
            requests.get(url)


def read_settings(path):
    config = ConfigParser.RawConfigParser()
    config.read(path)
    user = config.get('Authentication', 'user')
    password = config.get('Authentication', 'password')
    return {'user': user, 'password': password}


settings = read_settings('settings.cfg')


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


motion = MotionProcess()


@get('/motion/detection/start', apply=[authenticate])
def start_motion_detection():
    """Starts the motion process including detection for motion"""
    motion.start()


@get('/motion/detection/stop', apply=[authenticate])
def stop_motion_detection():
    """Stops the motion process"""
    motion.stop()


@get('/alerts/motion', apply=[authenticate])
def notify_device_about_motion():
    LOG.debug('Notify device about motion')


@get('/server/action/snapshot', apply=[authenticate])
def make_snapshot():
    LOG.debug('Request a current snapshot')
    motion.request_snapshot()


@get('/static/snapshots/<filename:re:.*\.jpg>', apply=[authenticate])
def send_snapshot(filename):
    return static_file(filename, root='snapshots', mimetype='image/jpg')


@post('/device/register', apply=[authenticate])
def register_device():
    id = request.forms.get('id')
    LOG.debug('Received id %s' % id)
    motion.set_device(id)


@post('/device/unregister', apply=[authenticate])
def unregister_device():
    id = request.forms.get('id')
    LOG.debug('Unregister device with id %s' % id)
    motion.set_device(None)


if __name__ == '__main__':
    run(host='localhost', port=4000)
