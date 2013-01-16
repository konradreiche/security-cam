from gcm import GCM
import datetime


class AlertNotifier(object):

    def __init__(self, settings, device):
        self.gcm = GCM(settings['gcm_api_key'])
        self.device = device

    def notify(self, filename):
        timestamp = datetime.datetime.now()
        timestamp = timestamp.strftime('%d. %B %Y at %I:%M%p')
        data = {'timestamp': timestamp, 'filename': filename}
        self.gcm.plaintext_request(registration_id=self.device, data=data)
