import requests
import util

settings = util.read_settings('settings.cfg')


def notify():
    auth = (settings['user'], settings['password'])
    requests.get('http://localhost:%d/alerts/motion'
                 % 4000, auth=auth)

if __name__ == '__main__':
    notify()
