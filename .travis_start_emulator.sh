#!/bin/sh

# Scripts starts Android emulator with name 'Default'

echo no | android create avd --force -n Default -t android-17 --abi armeabi-v7a
emulator -avd Default -skin 480x960 -no-audio -no-window &
