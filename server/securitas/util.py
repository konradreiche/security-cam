import ConfigParser
import fileinput


def read_settings(path):
    config = ConfigParser.RawConfigParser()
    config.read(path)
    user = config.get('Authentication', 'user')
    password = config.get('Authentication', 'password')
    gcm_api_key = config.get('GCM', 'api_key')

    for line in fileinput.input('motion.conf'):
        split = line.split('control_port')
        if len(split) is 2:
            control_port = int(split[1])
            break
    fileinput.close()

    return {'user': user, 'password': password, 'gcm_api_key': gcm_api_key,
            'control_port': control_port}
