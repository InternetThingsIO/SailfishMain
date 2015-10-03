#!/bin/sh
cd /opt/gitrepos/SailfishMain
git clean -df
git checkout master
git pull
service notice restart
