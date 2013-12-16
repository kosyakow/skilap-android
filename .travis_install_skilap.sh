#!/bin/sh

# Script installs skilap server

git clone https://github.com/kosyakow/skilapSnapshot.git
cd skilapSnapshot
npm install
node ./app.js &
cd ..
