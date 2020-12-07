package atlascollectiveint.custom;

import atlassharedclasses.*;
import atlasdsl.SensorType;
import java.util.Optional;

import atlascollectiveintgenerator.CollectiveInt;
import atlassharedclasses.ATLASSharedResult;

public class CustomCollectiveInt extends CollectiveInt {
  protected void handleMessage(ATLASSharedResult a) {
    super.handleMessage(a);
    if (a.getContentsClass() == SensorDetection.class) {
    	  Optional<SensorDetection> d_o = a.getSensorDetection();
    	  if (d_o.isPresent()) {
    		  SensorDetection d = d_o.get();
    		  if (d.getSensorType() == SensorType.SONAR) {
    		  	  ComputerCIshoreside.SONARDetectionHook(d_o.get(), (String)d.getField("robotName"));
    		  }
    	  }
      };

    if (a.getContentsClass() == GPSPositionReading.class) {
    		  Optional<GPSPositionReading> r_o = a.getGPSPositionReading();
    		  if (r_o.isPresent()) {
    			  GPSPositionReading r = r_o.get();
    			  ComputerCIshoreside.GPS_POSITIONDetectionHook(r.getX(),r.getY(), r.getRobotName());
    		  }
    	  }
    if (a.getContentsClass() == SensorDetection.class) {
    	  Optional<SensorDetection> d_o = a.getSensorDetection();
    	  if (d_o.isPresent()) {
    		  SensorDetection d = d_o.get();
    		  if (d.getSensorType() == SensorType.CAMERA) {
    		  	  ComputerCIshoreside.CAMERADetectionHook(d_o.get(), (String)d.getField("robotName"));
    		  }
    	  }
      };

  }

  public void init() {
    super.init();
    ComputerCIshoreside.init();
  }
}
