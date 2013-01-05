from sys import argv
import requests
import util

settings = util.read_settings('settings.cfg')


def notify(filename):
    auth = (settings['user'], settings['password'])
    payload = {'filename': filename}
    requests.post('http://localhost:%d/server/action/snapshot/ready'
                  % 4000, auth=auth, data=payload)

if __name__ == '__main__':
    if argv[1].endswith('-snapshot.jpg'):
        filename = argv[1].replace('captures/', '')
        notify(filename)
