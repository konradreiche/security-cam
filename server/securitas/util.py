"""
Utility functionality.
"""
import ConfigParser
import fileinput
import os


def read_settings(path):
    """
    Reads the settings from a configuration file which are necessary for
    establish required user authentication and push notification via Google
    Cloud Message Service (GCM).
    """

    config = ConfigParser.RawConfigParser()
    config.read(path)
    captures_path = config.get('Captures', 'directory')
    user = config.get('Authentication', 'user')
    password = config.get('Authentication', 'password')
    gcm_api_key = config.get('GCM', 'api_key')
    dropbox_api_key = config.get('Dropbox', 'api_key')
    dropbox_app_secret = config.get('Dropbox', 'app_secret')
    dropbox_access_type = config.get('Dropbox', 'access_type')

    if captures_path.startswith('~'):
        captures_path = captures_path.replace("~", os.path.expanduser("~"))

    if not os.path.exists(captures_path):
        os.makedirs(captures_path)

    default_path = '/usr/local/etc/security-cam/motion.conf'
    if os.path.exists(default_path):
        lines = fileinput.input(default_path)
    else:
        lines = fileinput.input('conf/motion.conf')

    for line in lines:
        split = line.split('control_port')
        if len(split) is 2:
            control_port = int(split[1])
            break
    fileinput.close()

    return {'captures_path': captures_path,
            'user': user,
            'password': password,
            'gcm_api_key': gcm_api_key,
            'control_port': control_port,
            'dropbox_api_key': dropbox_api_key,
            'dropbox_app_secret': dropbox_app_secret,
            'dropbox_access_type': dropbox_access_type}
