"""
Events related to the Motion process.
"""
from dropbox import client, session
from multiprocessing import Process, Queue
from os.path import basename
from watchdog.events import FileSystemEventHandler

import os
import webbrowser

TOKEN_FILE = '/tmp/dropbox_token_file'


class SnapshotEventHandler(FileSystemEventHandler):
    """
    An event handler for acting on snapshot file creations.
    """

    def __init__(self, motion_process):
        super(SnapshotEventHandler, self).__init__()
        self.motion_process = motion_process

        if self.has_dropbox_configuration(motion_process):
            self.authenticate_dropbox(motion_process.settings)

    def has_dropbox_configuration(self, motion_process):
        """
        Check whether the Dropbox configuration is complete. Otherwise the
        Dropbox authentication will be skipped.
        """

        settings = motion_process.settings
        return (settings['dropbox_api_key'] and
                settings['dropbox_app_secret'] and
                settings['dropbox_access_type'])

    def authenticate_dropbox(self, settings):
        """
        Authenticates the application for the user's Dropbox account.
        """

        db_session = session.DropboxSession(settings['dropbox_api_key'],
                                            settings['dropbox_app_secret'],
                                            settings['dropbox_access_type'])

        token_key, token_secret = self.retrieve_request_token(db_session)
        db_session.set_token(token_key, token_secret)
        self.dropbox_client = client.DropboxClient(db_session)
        self.queue = DropboxSynchronizer(self.dropbox_client).start()

    def retrieve_request_token(self, db_session):
        """
        Either creates a new request token or loads a previous from a file.
        """

        if os.path.exists(TOKEN_FILE):
            token_file = open(TOKEN_FILE)
            token_key, token_secret = token_file.read().split('|')
            token_file.close()
        else:
            request_token = db_session.obtain_request_token()
            url = db_session.build_authorize_url(request_token)

            save_output = os.dup(1)
            os.close(1)
            os.open(os.devnull, os.O_RDWR)
            try:
                webbrowser.open(url)
            finally:
                os.dup2(save_output, 1)

            print 'Please visit:', url
            raw_input("After authorization press 'Enter' to continue")

            access_token = db_session.obtain_access_token(request_token)
            token_file = open(TOKEN_FILE, 'w')
            token_file.write("%s|%s" % (access_token.key, access_token.secret))
            token_file.close()
            token_key, token_secret = access_token.key, access_token.secret

        return token_key, token_secret

    def on_modified(self, event):
        """
        Triggered when a snapshot is created. This event is used to notify the
        client about the final creation of snapshot without using busy waiting.
        """

        path = event.src_path
        filename = basename(path)
        if self.dropbox_client:
            self.queue.put((path, filename))

        if filename.endswith('snapshot.jpg'):
            self.motion_process.notify_about_snapshot(filename)
        elif filename == 'lastsnap.jpg':
            pass  # swallow
        else:
            self.motion_process.alert(filename)


class DropboxSynchronizer(object):
    """
    Consumer class for uploading newly created snapshots asynchronously.
    """

    def __init__(self, client):
        self.client = client
        self.queue = Queue()

    @staticmethod
    def serve(queue, client):
        """
        Serves the queue by retrieving current snapshot files.
        """

        while True:
            task = queue.get()
            if not task:
                break
            else:
                path, filename = task
                snapshot_file = open(path)
                client.put_file(filename, snapshot_file)
                snapshot_file.close()

    def start(self):
        """
        Starts the consumer process.
        """

        self.worker = Process(target=DropboxSynchronizer.serve,
                              args=(self.queue, self.client))
        self.worker.start()
        return self.queue
