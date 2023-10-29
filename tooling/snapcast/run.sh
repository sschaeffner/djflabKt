#!/bin/bash

mkdir /run/dbus
dbus-daemon --system --nosyslog --print-address
avahi-daemon --no-drop-root --no-chroot --no-rlimits --daemonize
/app/snapserver
