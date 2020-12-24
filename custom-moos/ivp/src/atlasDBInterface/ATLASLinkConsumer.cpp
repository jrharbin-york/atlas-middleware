#include <activemq/core/ActiveMQConnectionFactory.h>
#include <activemq/library/ActiveMQCPP.h>
#include <activemq/util/Config.h>
#include <cms/BytesMessage.h>
#include <cms/Connection.h>
#include <cms/ExceptionListener.h>
#include <cms/MapMessage.h>
#include <cms/MessageListener.h>
#include <cms/Session.h>
#include <cms/TextMessage.h>
#include <decaf/lang/Integer.h>
#include <decaf/lang/Long.h>
#include <decaf/lang/Runnable.h>
#include <decaf/lang/System.h>
#include <decaf/lang/Thread.h>
#include <decaf/util/concurrent/CountDownLatch.h>

#include <iostream>
#include <memory>
#include <regex>
#include <stdio.h>
#include <stdlib.h>

#include "ATLASLinkConsumer.h"

using namespace activemq::core;
using namespace decaf::util::concurrent;
using namespace decaf::util;
using namespace decaf::lang;
using namespace cms;
using namespace std;

ATLASLinkConsumer::ATLASLinkConsumer(ATLASDBInterface *atlas_int,
                                     const std::string &brokerURI,
                                     const std::string &atlas_link_extraname) {
  this->atlas_int = atlas_int;
  // Create a ConnectionFactory
  auto_ptr<ConnectionFactory> connectionFactory(
      ConnectionFactory::createCMSConnectionFactory(brokerURI));

  // Create a Connection
  connection = connectionFactory->createConnection();
  connection->start();
  // connection->setExceptionListener(this);

  // Create a Session
  if (this->sessionTransacted == true) {
    session = connection->createSession(Session::SESSION_TRANSACTED);
  } else {
    session = connection->createSession(Session::AUTO_ACKNOWLEDGE);
  }

  // Determine the topic name
  ostringstream topicname;
  if (!atlas_link_extraname.empty())
    topicname << atlas_link_extraname;


  // Create the destination (Topic or Queue)
  destination = session->createTopic(topicname.str());
  // Create a MessageConsumer from the Session to the Topic or Queue
  consumer = session->createConsumer(destination);
  consumer->setMessageListener(this);
}

ATLASLinkConsumer::~ATLASLinkConsumer() { cleanup(); }

void ATLASLinkConsumer::cleanup() {
  if (connection != NULL) {
    try {
      connection->close();
    } catch (cms::CMSException &ex) {
      ex.printStackTrace();
    }
  }

  // Destroy resources.
  try {
    delete destination;
    destination = NULL;
    delete consumer;
    consumer = NULL;
    delete session;
    session = NULL;
    delete connection;
    connection = NULL;
  } catch (CMSException &e) {
    e.printStackTrace();
  }
}

void ATLASLinkConsumer::onMessage(const Message *message) {
  try {
    const TextMessage *textMessage = dynamic_cast<const TextMessage *>(message);
    string text = "";

    if (textMessage != NULL) {
      text = textMessage->getText();
      smatch matches;

      std::regex tempRegex("(.+)\\|(\\w+)=(.+)");

      // Parse the text from the message
      // Message format e.g. (time_double)|m=v
      if (std::regex_search(text, matches, tempRegex)) {
        double endTime = stod(matches[1]);
        string key = matches[2];
        string val = matches[3];
        cout << "From ActiveMQ" << key << endl;
        CMOOSMsg *moosemsg = new CMOOSMsg(MOOS_STRING, key, val);
        if (atlas_int)
                atlas_int->fromMQHook(*moosemsg, endTime);
        delete moosemsg;
      } else {
        cout << "No match" << endl;
      }
    } else {
      cout << "Not a text message" << endl;
    }

  } catch (CMSException &e) {
    e.printStackTrace();
  } catch (invalid_argument &ia) {
    cout << "Exception: invalid time" << endl;
  }

  // Commit all messages.
  if (this->sessionTransacted) {
    session->commit();
  }

  // No matter what, tag the count down latch until done.
  // doneLatch.countDown();
}
