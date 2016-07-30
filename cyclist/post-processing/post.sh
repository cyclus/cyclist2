#!/bin/bash
FILES="cyclist reactor power flow capacity inventory"
BASE=${0/\/post.sh/}
TIMEFORMAT=%R

echo 'cyan post' 
time cyan -db $1 post

for f in $FILES; do 
	echo
	echo  $f.sql
	time -p sqlite3 $1 < ${BASE}/$f.sql
done
