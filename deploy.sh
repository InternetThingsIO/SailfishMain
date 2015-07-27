#!/bin/sh
cd /var/gitrepos/SailfishMain
git reset --hard
git checkout master
git pull
service sailfishnode restart
