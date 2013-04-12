"""
Notifier script for sending push notifications.
"""
from gcm import GCM
import datetime


class AlertNotifier(object):
    """
    Notifier for sending push notifications to a specified device.
    """

    def __init__(self, settings, device):
        self.gcm = GCM(settings['gcm_api_key'])
        self.device = device

    def notify(self, filename):
        """
        Notify about a certain filename, creates a timestamp in order inform
        the receive when the snapshot was created.
        """

        timestamp = datetime.datetime.now()
        timestamp = timestamp.strftime('%d. %B %Y at %I:%M%p')
        data = {'timestamp': timestamp, 'filename': filename}
        self.gcm.plaintext_request(registration_id=self.device, data=data)
