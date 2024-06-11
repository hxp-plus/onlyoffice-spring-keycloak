#!/bin/bash

touch /var/log/document-server.log
cd /opt/documentserver/server/FileConverter/
nohup bash -c 'LD_LIBRARY_PATH=$PWD/bin NODE_ENV=development-linux NODE_CONFIG_DIR=/etc/documentserver ./converter' 2>&1 >>/var/log/document-server.log &
cd /opt/documentserver/server/DocService/
nohup bash -c 'NODE_ENV=development-linux NODE_CONFIG_DIR=/etc/documentserver ./docservice' 2>&1 >>/var/log/document-server.log &
tail -f /var/log/document-server.log
