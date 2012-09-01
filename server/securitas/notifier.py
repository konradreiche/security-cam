from gcm import GCM

from tornado import httpserver, ioloop, web
from tornado.web import RequestHandler

import ConfigParser


class MainHandler(RequestHandler):
        
        def initialize(self, api_key):
                self.gcm = GCM(api_key)

        def post(self): 

                uri = self.request.uri
                if (uri == '/register'):
                        self.application.device = self.get_argument('registrationId')
                        print 'Registering Device %s' % self.application.device
                        
        def get(self):

                uri = self.request.uri
                if (uri == '/alarm'):
                        data = { 'message' : 'Alarm!' }
                        device = self.application.device
                        print 'Send Message to Device %s' % device
                        self.gcm.json_request(registration_ids = device, data = data)

                self.set_status(200)
                self.finish()


if __name__ == "__main__":
        
        config = ConfigParser.ConfigParser()
        config.read('key.cfg')

        api_key = config.get('API', 'key') 
        
        application = web.Application([
                (r'.*', MainHandler, dict(api_key = api_key)),
                ])
        
        application.listen(3030)
        ioloop.IOLoop.instance().start()
