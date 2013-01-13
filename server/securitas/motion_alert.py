from sys import argv
import requests
import util

settings = util.read_settings('etc/settings.cfg')


def notify(filename):
    auth = (settings['user'], settings['password'])
    data = {'filename': filename}
    requests.post('http://localhost:%d/alerts/motion'
                  % 4000, auth=auth, data=data)

if __name__ == '__main__':
    notify(argv[1])
