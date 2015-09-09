#!/bin/sh
cd /var/gitrepos/SailfishMain
git clean -df
git checkout master
git pull
service sailfishnode restart
