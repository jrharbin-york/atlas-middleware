#!/bin/sh

rm -rf /home/ubuntu/.ros/log

echo "Starting roslaunch launchers"
xterm -e /bin/bash -l -c "roslaunch safemuv_gazebo_simulation prepare_sim.launch" &
sleep 25
echo "Starting SAFEMUV Docker container"
xterm -e docker run --network host -v /home/$USER/catkin_ws/src/safemuv_ros/safemuv_shared/:/catkin_ws/src/safemuv_shared afi_core /bin/bash -c 'source /catkin_ws/devel/setup.bash && roslaunch safemuv_shared multi_afi_bringup.launch' &
xterm -e docker run -it --network host safemuv_metrics_3s &
sleep 15
echo "Starting roslaunch launchers"
xterm -e /bin/bash -l -c "roslaunch safemuv_rviz_visualization safemuv_simulation_visualization.launch" &
sleep 15
xterm -e /bin/bash -l -c "roslaunch safemuv_mission multi_mission.launch"
