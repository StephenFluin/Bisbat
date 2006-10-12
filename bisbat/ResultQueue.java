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
				Thread.sleep(100); // Optimize with semaphores? !TODO
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		Bisbat.print("Popping off: " + results.get(0));
		return results.remove(0);
	}
}
