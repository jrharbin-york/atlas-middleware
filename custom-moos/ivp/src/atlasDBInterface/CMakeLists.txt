#--------------------------------------------------------
# The CMakeLists.txt for:                ATLASDBInterface
#--------------------------------------------------------
# Set System Specific Libraries
if (${WIN32})
  SET(SYSTEM_LIBS
    wsock32)
else (${WIN32})
  SET(SYSTEM_LIBS
    m
    pthread)
endif (${WIN32})

SET(SRC
  ATLASDBInterface_App.cpp
  ATLASDB_Info.cpp
  ATLASLinkApp.cpp
  ATLASLinkConsumer.cpp
  ATLASLog.cpp
  main.cpp)

set(CMAKE_CXX_STANDARD 11)

set(APR_INCLUDE_DIR "/home/userfs/j/jrh512/source/apr-install/include/apr-1")
#set(ActiveMQ_INCLUDE_DIR "/home/userfs/j/jrh512/source/activemq-cpp-install/include/activemq-cpp-3.9.5")
set(ActiveMQ_INCLUDE_DIR "/usr/local/include/activemq-cpp-3.9.5")
include_directories(${APR_INCLUDE_DIR} ${ActiveMQ_INCLUDE_DIR})


#find_library(ActiveMQ_LIBRARY
#  NAMES activemq-cpp)
#set(ActiveMQ_LIBRARY "/home/userfs/j/jrh512/source/activemq-cpp-install/lib/libactivemq-cpp.so")
set(ActiveMQ_LIBRARY "/usr/local/lib/libactivemq-cpp.so")
set(SSL_LIBRARY "/usr/lib/x86_64-linux-gnu/libssl.so")

ADD_EXECUTABLE(ATLASDBInterface ${SRC})

TARGET_LINK_LIBRARIES(ATLASDBInterface
  ${MOOS_LIBRARIES}
  ${ActiveMQ_LIBRARY}
  mbutil
  ${SYSTEM_LIBS}
  ${SSL_LIBRARY}
)

