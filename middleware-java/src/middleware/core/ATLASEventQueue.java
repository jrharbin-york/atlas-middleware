package middleware.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import atlassharedclasses.ATLASObjectMapper;

public abstract class ATLASEventQueue<E> extends ArrayBlockingQueue<E> implements Runnable {
	private static final long serialVersionUID = 1L;
	protected ATLASObjectMapper atlasOMapper;
	protected ATLASCore core;
	
	protected boolean continueLoop = true;
	protected char progressChar;
	protected List<VoidLambda> afterHooks = new ArrayList<VoidLambda>();
	protected int eventCount = 0;
	protected final int CHAR_COUNT_LIMIT = 320;
	protected final int EVENTS_PER_PRINTED_CHAR = 8;
	
	public ATLASEventQueue(ATLASCore core, int capacity, char progressChar) {
		super(capacity);
	}
	
	public void registerAfterHook(VoidLambda v) {
		afterHooks.add(v);
	}
	
    public static void startThread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }
	 
	public abstract void setup();
	
	private void printCharIfReady() {
		eventCount++;
		if (eventCount % EVENTS_PER_PRINTED_CHAR == 0) {
			System.out.print(progressChar);
		}
		if (eventCount > CHAR_COUNT_LIMIT) {
			System.out.println();
			eventCount = 0;
		} 
	}
	
	public void run() {
		System.out.println("run() in ATLASEventQueue - " + this.getClass().getName());
		while (continueLoop) {
			try {
				E e = take();
				printCharIfReady();
				
				for (VoidLambda v : afterHooks) {
					v.op();
				}

				if (e != null) {
					handleEvent(e);
					// TODO: put logger calls here for the new event
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public abstract void handleEvent(E event);
}
