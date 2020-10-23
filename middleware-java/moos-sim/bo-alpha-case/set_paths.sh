#!/bin/sh
if [ `hostname` = "atlas-VirtualBox" ]; then
	export MOOS=/home/atlas/atlas/atlas-middleware/custom-moos
	export PATH=$MOOS/bin/:$PATH
	export PATH=$MOOS/ivp/bin:$PATH
fi


