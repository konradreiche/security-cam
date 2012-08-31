from gcm import GCM

from tornado import httpserver, ioloop

import ConfigParser

def register_identifier(request):
        message = "HTTP Requested %s\n" % request.method
        print message
        request.finish()


config = ConfigParser.ConfigParser()
config.read('key.cfg')

API_KEY = config.get('API', 'key')

http_server = httpserver.HTTPServer(register_identifier)
http_server.listen(3030)
ioloop.IOLoop.instance().start()

gcm = GCM(API_KEY)
data = { 'message' : 'Alarm!' }
