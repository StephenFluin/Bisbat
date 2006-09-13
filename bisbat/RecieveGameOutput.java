/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;

public class RecieveGameOutput extends Thread {
	/* Thread: constantly prints a given input stream */
	
	private BufferedReader reader;

	public RecieveGameOutput (InputStreamReader i) {
		reader = new BufferedReader(i);
	}
	
	public void run(){
		String line = "Starting PrintSteam";
		while (line != null){
			try {
				if(!line.equals("")) {
					System.out.println(line);
				}
				line = reader.readLine();
			} catch (IOException e) {
				System.err.println("PrintSteam failed");
				e.printStackTrace();
			}
		}
		System.out.println("PrintSteam closed");
	}
}

