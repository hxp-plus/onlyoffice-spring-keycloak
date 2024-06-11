#!/bin/bash
while read line;do
  docker save $line -o $(echo ${line##*/}|tr ':' '_').tar
done < images.txt
for i in *.tar;do
  gzip $i
done