#!/bin/sh
cd /home/jharbin/academic/atlas/atlas-middleware/middleware-java
javac -d /home/jharbin/academic/atlas/atlas-middleware/middleware-java/target/classes/ -classpath .:/home/jharbin/academic/atlas/atlas-middleware/middleware-java/target/classes/ src/atlasdsl/loader/GeneratedDSLLoader.java
