import requests
from securitas import util
from sys import argv

settings = util.read_settings('settings.cfg')


def notify():
    auth = (settings['user'], settings['password'])
    requests.get('http://localhost:%d/server/action/snapshot/ready'
                 % 4000, auth=auth)

if __name__ == '__main__':
    if argv[1].endswith('-snapshot.jpg'):
        notify()
