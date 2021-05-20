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

A video of the platform in operation is available [here](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/atlas-ci-testing-casestudy1.mp4 "Video of case study 1")

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
![alt text](https://github.com/jrharbin-york/atlas-middleware/blob/ciexpt/images-and-videos/screenshot-image.png "Case Study image")

The models for this are located in the 

Set up the run configuration at:
