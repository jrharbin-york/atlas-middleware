package atlassharedclasses;

public class SpeedReading extends SensorInfo {
        private double speed;
        private String robotName;

        // There has to be a default constructor to allow the class to be 
        // serialised.
        public SpeedReading() {

        }

        public SpeedReading(double speed, String robotName) {
                this.speed = speed;
                this.robotName = robotName;
        }

        public SpeedReading(double x, double y, double z, double speed, String robotName) {
                this.speed = speed;
                this.robotName = robotName;
        }

        public SpeedReading(Point pos, double speed, String robotName) {
                this.speed = speed;
                this.robotName = robotName;
        }

        public String getRobotName() {
                return robotName;
        }

        public double getSpeed() {
                return speed;
        }
}
