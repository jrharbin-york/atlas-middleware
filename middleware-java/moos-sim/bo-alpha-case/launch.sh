#!/bin/sh

. ./set_paths.sh
pAntler targ_shoreside.moos > console-logs/shoreside-out 2> console-logs/shoreside-err  &
pAntler targ_henry.moos > console-logs/henry-out 2> console-logs/henry-err &
pAntler targ_gilda.moos > console-logs/gilda-out 2> console-logs/gilda-err &
