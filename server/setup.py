#!/usr/bin/env python

from distutils.core import setup

setup(name='security-cam',
      version='0.1',
      description='Attachment for Motion to set up a security system',
      author='Konrad Reiche',
      author_email='konrad.reiche@gmail.com',
      url='http://www.konrad-reiche.com/security-cam',
      license='mit',
      packages=['securitas'],
      data_files=[('etc/security-cam',
                   ['securitas/conf/motion.conf.example',
                    'securitas/conf/settings.cfg.example'])],
      keywords=['application', 'backend', 'motion'],
      classifiers=[
          'Programming Language :: Python',
          'License :: OSI Approved :: MIT License',
          'Operating System :: Unix',
          'Development Status :: 4 - Beta',
          'Intended Audience :: Developers',
          'Intended Audience :: End Users/Desktop',
          'Intended Audience :: System Administrators',
          'Topic :: Internet :: WWW/HTTP :: HTTP Servers',
          'Topic :: Multimedia :: Video'],
      long_description="""A REST API on top of Motion.

      Motion is a program that monitors the video signal from cameras. This
      utility software is an attachment on top of Motion to offer a set of API
      commands. These commands are thought to be used together with the client.
      The client is an Android application which allows to remotely control the
      motion detection and sends push notifications and snapshots the
      client."""
      )
