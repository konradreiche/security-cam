from bottle import get, post, request, run, static_file
import logging

LOG = logging.getLogger(__name__)
LOG.setLevel(logging.DEBUG)
LOG.addHandler(logging.StreamHandler())


@get('/motion/detection/start')
def start_motion_detection():
    LOG.debug('Start Motion')


@get('/motion/detection/stop')
def stop_motion_detection():
    LOG.debug('Kill Motion')


@get('/alerts/motion')
def notify_device_about_motion():
    LOG.debug('Notify device about motion')


@get('/server/action/snapshot')
def make_snapshot():
    LOG.debug('Makes a current snapshot')


@get('/static/snapshots/<filename:re:.*\.jpg>')
def send_snapshot(filename):
    return static_file(filename, root='snapshots', mimetype='image/jpg')


@post('/device/register')
def register_device():
    id = request.forms.get('id')
    LOG.debug('Received id %s' % id)


@post('/device/unregister')
def unregister_device():
    id = request.forms.get('id')
    LOG.debug('Unregister device with id %s' % id)

if __name__ == '__main__':
    run(host='localhost', port=4000)
