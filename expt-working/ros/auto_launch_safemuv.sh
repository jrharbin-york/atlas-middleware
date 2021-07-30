#!/bin/sh

TRAJ_HOST_LOCATION=/home/jharbin/catkin_ws/src/safemuv/traj_plan_external/trajectory_planner_external.yaml
TRAJ_DEST_LOCATION=/catkin_ws/src/planning_stack/packages/path_planning/path_planner_geometric_primitives/trajectory_planner_geometric_primitives_ros/config/path_planner_geometric_primitives_3d_airplane_cargolux_airplane_frame.yaml

rm -rf /home/jharbin/.ros/log

# TODO: reduce the time delays
echo "Starting SAFEMUV Docker containers"
# To setup the images for the first run
#docker run --name SAFEMUV1 -it --network host -v /home/jharbin/catkin_ws/src/safemuv/safemuv_shared:/catkin_ws/src/safemuv_shared afi_base_software_multi bash
#docker run --name SAFEMUV2 -it --network host -v /home/jharbin/catkin_ws/src/safemuv/safemuv_shared:/catkin_ws/src/safemuv_shared afi_base_software_multi bash
docker start SAFEMUV1
docker start SAFEMUV2
sleep 10
#docker cp $TRAJ_HOST_LOCATION SAFEMUV1:$TRAJ_DEST_LOCATION
#docker cp $TRAJ_HOST_LOCATION SAFEMUV2:$TRAJ_DEST_LOCATION
sleep 1
echo "Starting roslaunch launchers"
xterm -e /bin/bash -l -c "roslaunch safemuv_gazebo_simulation prepare_sim.launch" &
sleep 5
xterm -e /bin/bash -l -c "roslaunch safemuv_launchers safemuv_launch_obstacle_publisher.launch" &
sleep 5
xterm -e /bin/bash -l -c "roslaunch safemuv_launchers safemuv_visualizator.launch" &
xterm -e /bin/bash -l -c "docker exec -it SAFEMUV1 bash -c 'source /catkin_ws/devel/setup.bash && roslaunch safemuv_shared afi_uav_1.launch'" &
xterm -e /bin/bash -l -c "docker exec -it SAFEMUV2 bash -c 'source /catkin_ws/devel/setup.bash && roslaunch safemuv_shared afi_uav_2.launch'" &
xterm -e /bin/bash -l -c "docker run -it --network host safemuv_metrics" &
sleep 10
xterm -e /bin/bash -l -c "rosrun safemuv_mission state_machine.py" &
xterm -e /bin/bash -l -c "rosrun safemuv_mission state_machine_2.py"

# in the new version, only launch 1 container
# Pedro's new version

