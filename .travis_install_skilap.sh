#!/bin/sh

# Script installs skilap server

git clone https://github.com/kosyakow/skilapSnapshot.git
cd skilapSnapshot
npm install
sudo node ./app.js &
cd ..
