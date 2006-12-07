package bisbat;

import java.util.Vector;

public class ResultQueue {
	
	private Vector<String> results;
	private boolean listening;
	
	public ResultQueue() {
		results = new Vector<String>();
		listening = false;
	}
	
	public void add(String s) {
		if(!listening) {
			notMine(s);
		} else {
			results.add(s);
		}
	}
	
	/**
	 * Insert a command at the front of the queue.
	 * @param s The command we are inserting.
	 */
	public void insert(String s) {
		results.add(0,s);
	}
	public String pop() {
		while(results.size() <= 0) {
			try {
				Thread.sleep(100); // Optimize with semaphores? !TODO
				//System.out.println("ResultQueue has been asked to pop, waiting for something to pop.");
			} catch(Exception e) {
				System.err.println("Failed in ResultQueue.pop()");
				e.printStackTrace();
			}
		}
		//Bisbat.debug("Pop> " + results.get(0)); // debugger
		return results.remove(0);
	}

	public void listen() {
		listening = true;
	}
	public void unlisten() {
		listening = false;
	}
	public void notMine(String msg) {
		System.out.println("<--'" + msg + "'"); 
	}

}
