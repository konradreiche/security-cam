"""Events related to the Motion process."""
from os.path import basename
from watchdog.events import FileSystemEventHandler


class SnapshotEventHandler(FileSystemEventHandler):
    """An event handler for acting on snapshot file creations."""

    def __init__(self, motion_process):
        super(SnapshotEventHandler, self).__init__()
        self.motion_process = motion_process

    def on_modified(self, event):
        filename = basename(event.src_path)
        if filename.endswith('snapshot.jpg'):
            self.motion_process.notify_about_snapshot(filename)
        elif filename == 'lastsnap.jpg':
            # swallow
            pass
        else:
            self.motion_process.alert(filename)
