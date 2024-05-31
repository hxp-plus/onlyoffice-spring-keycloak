#!/bin/bash

service nginx restart
service postgresql restart
service rabbitmq-server restart

touch /logs.txt

cd /var/www/html/documentserver/server/FileConverter/
nohup bash -c 'LD_LIBRARY_PATH=$PWD/bin NODE_ENV=development-linux NODE_CONFIG_DIR=$PWD/../Common/config ./converter' 2>&1 >>/logs.txt &
cd /var/www/html/documentserver/server/DocService/
nohup bash -c 'NODE_ENV=development-linux NODE_CONFIG_DIR=$PWD/../Common/config ./docservice' 2>&1 >>/logs.txt &

tail -f /logs.txt
