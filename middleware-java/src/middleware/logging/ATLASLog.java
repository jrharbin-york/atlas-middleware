package middleware.logging;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import atlasdsl.GoalAction;

public class ATLASLog {
	private static ATLASLog logger = null;
	private FileWriter carsInboundLog;
	private FileWriter carsOutboundLog;
	private FileWriter mqOutboundLog;
	private FileWriter fuzzingLog;
	private FileWriter goalLog;
	private FileWriter timeLog;
	private FileWriter optionsLog;
	private FileWriter positionLog;
	private FileOutputStream timeStream;
	
	ATLASLog() {
		try {
			carsInboundLog = new FileWriter("logs/atlasCARSInbound.log");
			carsOutboundLog = new FileWriter("logs/atlasCARSOutbound.log");
			mqOutboundLog = new FileWriter("logs/atlasMQOutbound.log");
			fuzzingLog = new FileWriter("logs/fuzzing.log");
			goalLog = new FileWriter("logs/goalLog.log");
			timeStream = new FileOutputStream("logs/atlasTime.log");
			optionsLog = new FileWriter("logs/options.log");
			String fileNameDateTag = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			positionLog = new FileWriter("logs/robotPositionTimeSeries-" + fileNameDateTag + ".log");
			timeLog = new FileWriter(timeStream.getFD());
			timeLog.write("0.0\n");
			timeLog.flush();
			timeStream.getFD().sync();
			
			System.out.println("FileWriters created");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static ATLASLog getLog() {
		if (logger == null) {
			logger = new ATLASLog();
		} 
		return logger;
	}
	
	public static synchronized void logFuzzing(String msg) {
		try {
			FileWriter w = getLog().fuzzingLog;
			w.write(msg + "\n");
			w.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logCARSInbound(String queueName, String text) {
		try {
			getLog().carsInboundLog.write("Received from CARS queue " + queueName + ":" + text + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logCARSOutbound(String queueName, String text) {
		try {
			getLog().carsOutboundLog.write("Sending to CARS queue " + queueName + ":" + text + "\n");
			// TODO: remove this flush to improve performance
			getLog().carsOutboundLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logActiveMQOutbound(String queueName, String text) {
		try {
			getLog().mqOutboundLog.write("ActiveMQProducer.send on " + queueName + " received textMessage: " + text + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void logGoalMessage(GoalAction ga, String msg) {
		try {
			String className = ga.getClass().getName();
			getLog().goalLog.write(className + "," + msg + "\n");
			// Ensure the stream is flushed as for some reason it wasn't 
			// ever updating the file, even though all the other log files are?
			// The writes should be infrequent though, so it shouldn't matter
			getLog().goalLog.flush();
			System.out.println("logGoalMessage:" + msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logCIInbound(String queueName, String text) {

	}
	
	public static void logMiddlewareOptions(String key, String value) {
		try {
			ATLASLog l = getLog();
			l.optionsLog.write(key + "=" + value);
			l.optionsLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logTime(double timeVal) {
		try {
			ATLASLog l = getLog();
			l.timeLog.write(String.valueOf(timeVal) + "\n");
			// Ensure the stream is flushed. If it isn't, experiments time
			// will not be updated regularly enough
			l.timeLog.flush();
			l.timeStream.getFD().sync();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logPosition(double time, String robot, double x, double y) {
		ATLASLog l = getLog();
		try {
			l.positionLog.write(time + "," + robot + "," + x + "," + y + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
