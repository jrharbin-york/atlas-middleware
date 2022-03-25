#!/bin/sh
JC=`cat java_cmd_string`
xterm -hold -e /bin/bash -l -c "$JC atlascollectiveintgenerator.runner.CollectiveIntRunner $1"
