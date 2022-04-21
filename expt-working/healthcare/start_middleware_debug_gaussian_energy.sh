#!/bin/sh
JC=`cat java_cmd_string`
xterm -hold -e /bin/bash -l -c "$JC test.middleware.ROSLauncher $1 gui | grep gaussian"
