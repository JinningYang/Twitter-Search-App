#!/bin/bash

free_memory=`awk '/^Mem/ {print $7}' <(free -m)`

if [ $free_memory -le $((24 * 1024)) ]; then
	echo "Need at least 24G of memory to pass all tests."
	exit 127
fi

which mvn > /dev/null 2>&1
ret=$?
if [ "$ret" -ne "0" ]; then
	echo "Can not find Maven in $PATH".
	exit 127
fi

mvn clean test
