from gcm import GCM

from tornado import httpserver, ioloop, web
from tornado.web import RequestHandler

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
                if (uri == '/register'):
                        self.control.device = self.get_argument('registrationId')
                        print 'Registering Device %s' % self.application.device
                elif (uri == '/unregister'):
                        self.control.device = None
                        print 'Unregistering device'
                        
        def get(self):

                uri = self.request.uri
                if (uri == '/alarm' and self.control.device != None):
                        data = { 'message' : 'Alarm!' }
                        device = self.control.device
                        print 'Send Message to Device %s' % device
                        self.gcm.plaintext_request(registration_id = device, data = data)

                elif (uri == '/start'):
                        print 'Start detection..'
                        if (self.control.process == None):
                                process = subprocess.Popen(['motion', '-c', '/etc/motion/motion.conf'])
                                self.control.process = process

                elif (uri == '/stop'):
                        print 'Shutting detection down..'
                        if (self.control.process != None):
                                self.control.process.terminate()
                                self.control.process = None

                self.set_status(200)
                self.finish()


if __name__ == "__main__":
        
        config = ConfigParser.ConfigParser()
        config.read('key.cfg')

        api_key = config.get('API', 'key') 
        gcm = GCM(api_key)
        control = ProcessControl()
        
        application = web.Application([
                (r'.*', MainHandler, dict(gcm = gcm, control = control)),
                ])
        
        application.listen(3030)
        ioloop.IOLoop.instance().start()
