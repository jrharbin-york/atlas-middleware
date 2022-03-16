package atlascollectiveintgenerator;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import atlasdsl.*;
import atlassharedclasses.*;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import atlascollectiveint.logging.CollectiveIntLog;

public class CollectiveIntActiveMQProducer {
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination destination;
	private MessageProducer producer;
	private String queueName;
	private QueueOrTopic type;
	
	private ATLASObjectMapper aoMapper = new ATLASObjectMapper();

	public enum QueueOrTopic {
		QUEUE,
		TOPIC
	}
	
	CollectiveIntActiveMQProducer(String queueName, Mission mission) {
		this.queueName = queueName;
		this.type = QueueOrTopic.TOPIC;
	}
	
	public void setupConnection() {
		try {
			// Create a ConnectionFactory
			connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616)");

			// Create a Connection
			connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			System.out.println("CollectiveIntActiveMQProducer queueName = " + queueName);
			// Create the destination (Topic or Queue)
			if (type == QueueOrTopic.QUEUE) 
				destination = session.createQueue(queueName);
			
			if (type == QueueOrTopic.TOPIC)
				destination = session.createTopic(queueName);

			// Create a MessageProducer from the Session to the Topic or Queue
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}
	
	public void close() {
		// Clean up
		try {
			session.close();
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg) {
		try {
			TextMessage message = session.createTextMessage(msg);
			CollectiveIntLog.logCIGeneration(this.queueName, msg);
			producer.send(message);
		} catch (JMSException e) {
			CollectiveIntLog.logCIExceptions(e);
		}
	}
	
	public void send(BehaviourCommand cmd, String robotName) {
		try {
			CIEvent e = new CIEvent(cmd, robotName);
			String msg = aoMapper.serialise(e);
			sendMessage(msg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
