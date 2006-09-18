/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.SocketException;

public class RecieveGameOutput extends Thread {
	/* Thread: constantly prints a given input stream */
	
	private BufferedReader reader;
	private Bisbat bisbat;

	public RecieveGameOutput (Bisbat bisbat, InputStreamReader i) {
		this.bisbat = bisbat;
		reader = new BufferedReader(i);
	}
	
	public void run(){
		String line = "Starting PrintSteam";
		String buffer = "";
		while (line != null){
			try {
				
				line = reader.readLine();
				
				if(line == null) {
					// Then we are done reading lines (output from game is closed.)
					return;
				} else if(!line.equals("")) {
					line = decolor(line);
					buffer += line;
					if(line.matches(bisbat.getPromptMatch())) {
						//Handle the buffer then clear it.
						
						handleOutput(buffer);
						buffer = "";
					} else {
						//System.out.println("Line(' " + line + " ' != '"  + bisbat.getPromptMatch()+ "'.");
					}
					System.out.println("<-" + line);
					
				}
				
			} catch (SocketException e) {
				return;
			} catch (IOException e) {
				System.err.println("PrintSteam failed");
				e.printStackTrace();
			}
		}
	}
	
	// Eliminates color information sent from the game.  We may 
	// want to remove this later (or just change the color info to 
	// something readable).
	public String decolor(String s) {
	    char ESCAPE = '\033';
        s = s.replaceAll(ESCAPE + "\\[[01];[0-9][0-9]m", "");
        

        return s;
	}
	public void handleOutput(String s) {
		// Load info into a room if found.
		if(s.matches("<>(.*)<>\n(.*)Exits:(.*).") ) {
			//System.out.println("I see a room!");
		} else {
			//System.out.println("I don't see a room!");
		}
	}
}

