# ATLAS Platform
## Introduction

The ATLAS platform provides the ability to configure a multi-robot system (MRS) with a domain-specific language (DSL). 

The ATLAS middleware itself enables separation of concerns between the collective intelligence (CI) and the target robotic CARS simulator. Currently the 
middleware is implemented for the MOOS simulator and case studies incorporating UUV search-and-detections and obstacle avoidance are implemented. 

The CI receives information about the MRS status via the middleware, executes its logic, and relays back its decisions to the MRS via the middleware. 

Also, the middleware uses runtime monitoring to assess the status of goals defined in the DSL. 

This internal state, including goal events, is stored in log files that allow the computation of relevant mission metrics and post hoc analysis.

The low coupling between the CI and the MRS simulator, mediated by the middleware, enables not only to experiment easily with several candidate CI 
algorithms but also reinforces maintainability and extensibility (e.g., the components can be computing platform and programming language independent)

![alt text](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/methodologyV.png "ATLAS Methodology")
![alt text](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/architecture3.png "ATLAS Architecture")

A video of the platform in operation is available [here](https://www.dropbox.com/s/uprxwkcljbhdxv5/atlas-ci-testing-casestudy1.mp4?dl=0 "Video of case study 1")

## Dependencies
* ActiveMQ
* CARS simulators (MOOS-IvP simulator/ROS)
* ActiveMQ CMS Client (C++ interface)
* Eclipse Modelling Framework
* Epsilon
* Emfatic

## Installation instructions
* [Ubuntu installation](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/install-instructions/ubuntu-install.org "Ubuntu Installation instructions")
* [Eclipse additional instructions](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/install-instructions/eclipse-setup.org "Eclipse additional instructions")

## Case Studies
### Case Study 1
This is a search and locate scenario incorporating a maximum of four vehicles,
which scan using a sonar sensor and mutually verify detections of objects. The
variation programs in the model selecting three of the four available robots,
with either a wide or narrow sensor for each vehicle. This produces 48 different
configurations

![alt
text](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/screenshot-image.png
"Case Study image")

* ![Mission basis model](middleware-java/experiment-models/casestudy1/mission-basis.model "Mission basis model")
* ![Collective Intelligence Standard](middleware-java/src/atlascollectiveint/expt/casestudy1/ComputerCIshoreside_standard.java "Collective Intelligence Standard")
* ![Collective Intelligence Advanced](middleware-java/src/atlascollectiveint/expt/casestudy1/ComputerCIshoreside_advanced.java "Collective Intelligence Advanced")

### Case Study 2
This is based upon the MOOS bo-alpha mission. It incorporates obstacle avoidance
and inter-robot avoidance as the two vehicles scan specific areas and take
measurements, periodically alternating and crossing a central region which
contains numerous obstacles

![alt text](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/casestudy2-screenshot.png "Case Study image")

* ![Mission basis model](middleware-java/experiment-models/casestudy2/mission-basis.model "Mission basis model")
* ![Collective Intelligence Standard](middleware-java/src/atlascollectiveint/expt/casestudy2/ComputerCIshoreside_standard.java "Collective Intelligence Standard")
* ![Collective Intelligence Energy Tracking](middleware-java/src/atlascollectiveint/expt/casestudy2/ComputerCIshoreside_energytracking.java "Collective Intelligence Energy Tracking")

