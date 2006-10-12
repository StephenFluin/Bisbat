/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.SocketException;
import java.util.Vector;
import java.util.regex.*;

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
					
					if(line.matches(bisbat.getPromptMatch())) {
						//Handle the buffer then clear it.
						System.out.println("Found the prompt!  Handling contents of buffer.");
						handleOutput(buffer);
						buffer = "";
					} else {
						buffer += line;
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
		//Bisbat.print("handling output");
		//System.out.println(s);
		Pattern roomPattern = Pattern.compile(".*<>(.*)<>(.*)Exits:([^\\.]*)\\.(.*)$" );
		Matcher roomMatcher = roomPattern.matcher(s);
		if(roomMatcher.matches()) {
			System.out.println("~~~~~ Found a Room! ~~~~~"); // debugger
			String title = roomMatcher.group(1);
			String description =roomMatcher.group(2);
			String exits = roomMatcher.group(3);
			String beingsAndObjects = roomMatcher.group(4);
			
			String[] roomOccupants = beingsAndObjects.split("\n");
			//System.out.println("Beings and objects = '" + beingsAndObjects + "'.");
			Vector<Being> beings = new Vector<Being>();
			Vector<Item> items = new Vector<Item>();
			for(String occupant : roomOccupants) {
				System.out.println("occupant:" + occupant);
				if(occupant.startsWith("M:")) {
					Being b = new Being(occupant.substring(2));
					beings.add(b);
					bisbat.addKnowledgeOf(b);
				} else if(occupant.startsWith("I:")) {
					Item i = new Item(occupant.substring(2));
					items.add(i);
					bisbat.addKnowledgeOf(i);
				}
			}
			
			
			Room recentlyDiscoveredRoom = new Room(title, description, exits, beings, items);
			bisbat.foundRoom(recentlyDiscoveredRoom);
		} else {
			System.out.println("<--" + s);
			bisbat.resultQueue.add(s);
		}
		//Bisbat.print("Done handlign output.");
	}
}

