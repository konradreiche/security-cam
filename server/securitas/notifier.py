from gcm import GCM

from tornado import httpserver, httpclient, ioloop, web
from tornado.web import RequestHandler, StaticFileHandler

import ConfigParser
import subprocess
import os

class ProcessControl(object):
        """Stores information to control the Motion process"""

        def __init__(self):
                """Initializing the required attributes"""
                self.device = None
                self.process = None
                

class MainHandler(RequestHandler):
        
        def initialize(self, gcm, control):
                self.gcm = gcm
                self.control = control

        def post(self): 

                uri = self.request.uri
                print uri
                if (uri == '/control/register'):
                        self.control.device = self.get_argument('registrationId')
                        print 'Registering Device %s' % self.control.device
                elif (uri == '/control/unregister'):
                        self.control.device = None
                        print 'Unregistering device'
                        
        def get(self):

                if (self.control.device is not None):

                        uri = self.request.uri
                        if (uri.startswith('/control/alarm')):

                                picturePath = self.get_arguments('picture')
                                data = { 'picture' : picturePath[0] }
                                device = self.control.device
                                print 'Send Message to Device %s' % device
                                self.gcm.plaintext_request(registration_id = device, data = data)

                        elif (uri == '/control/start'):
                                print 'Start detection..'
                                if (self.control.process == None):
                                        process = subprocess.Popen(['motion', '-c', '/etc/motion/motion.conf'])
                                        self.control.process = process

                        elif (uri == '/control/stop'):
                                print 'Shutting detection down..'
                                if (self.control.process != None):
                                        self.control.process.kill()
                                        self.control.process = None

                        elif (uri == '/control/snapshot'):
                                if (self.control.process is not None):
                                       httpClient = httpclient.HTTPClient() 
                                       try:
                                                response = httpClient.fetch('http://localhost:8080/0/action/snapshot')
                                       except httpclient.HTTPERror, e:
                                               print 'Error:', e
                                       httpClient.close()

                self.set_status(200)
                self.finish()


if __name__ == "__main__":
        
        config = ConfigParser.ConfigParser()
        config.read('key.cfg')

        api_key = config.get('API', 'key') 
        gcm = GCM(api_key)
        control = ProcessControl()
       
        application = web.Application([
                (r'/control/.*', MainHandler, dict(gcm = gcm, control = control)),
                (r'/picture/(.*)', StaticFileHandler, { 'path' : 'picture' })
                ],
                )

        application.listen(3030)
        ioloop.IOLoop.instance().start()
