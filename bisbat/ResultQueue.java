package bisbat;

import java.util.Vector;

public class ResultQueue {
	
	private Vector<String> results;
	
	public ResultQueue() {
		results = new Vector<String>();
	}
	
	public void add(String s) {
		results.add(s);
	}
	
	public String pop() {
		while(results.size() <= 0) {
			try {
				Thread.sleep(1000); // Optimize with semaphores? !TODO
				System.out.println("We're caught right here, with results.size() = " + results.size());
			} catch(Exception e) {
				System.err.println("Failed in ResultQueue.pop()");
				e.printStackTrace();
			}
		}
		//Bisbat.print("Popping off: " + results.get(0)); // debugger
		return results.remove(0);
	}
}
