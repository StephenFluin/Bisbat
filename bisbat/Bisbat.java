package bisbat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bisbat extends Thread {

	static GregorianCalendar cal;
	
	public Connection connection;
	public String name;
	public String password;
	private String prompt;
	public Room currentRoom;
	
	public Room referenceRoom; //the one room we must assume to be true;
	public Vector<Being> knownBeingList;
	public Vector<Item> knownItemList;
	public Vector<Item> carrying;
	public RoomFinder roomFindingThread;
	public ResultQueue resultQueue;
	public Stack<Pair<String,Object>> toDoList;
	public boolean interrupted = false;
	
	// Character Variables
	public boolean hungry = false;
	public boolean thirsty = false;
	public int health, maxHealth, mana, maxMana, move, maxMove;
	public int boredom;
	public int experience;
	public int killThreshhold = 1;
	public boolean leveledRecently = false;
	
	
	
	
	/**
	 * Constructs a new instance of Bisbat
	 * @param loginName: Name of this instance of Bisbat
	 * @param loginPassword: Password used by this instance of Bisbat to login to the game
	 */
	public Bisbat(String loginName, String loginPassword) {
		name = loginName;
		password = loginPassword;
		prompt = "";
		knownBeingList = new Vector<Being>();
		knownItemList = new Vector<Item>();
		carrying = new Vector<Item>();
		roomFindingThread = new RoomFinder(this);
		roomFindingThread.start();
		resultQueue  = new ResultQueue();
	 	toDoList = new Stack<Pair<String,Object>>();

	 	// Set initial objectives
	 	toDoList.push(new Pair<String,Object>("survive", null));
	 	toDoList.push(new Pair<String,Object>("reference", null));
	 	toDoList.push(new Pair<String,Object>("explore", null));
	 	leveledRecently = false;
	}

	/**
	 * Creates a connection to the world, and logs Bisbat in.
	 * Determines Bisbats actions based on the toDoList
	 */
	public void run() {
		// Connect to the world, and login
		connection = new Connection(this, "www.mortalpowers.com", 4000);
	 	login();
	 	
	 	//print out the start of the clock.
	 	print("Starting objectives");
	 	

	 	
	 	// Determine next objective and act accordingly
	 	while(toDoList.size() > 0) {
	 		Pair<String,Object> toDoItem = toDoList.pop();
	 		interrupted = false;
	 		if(toDoItem.left.equals("survive")) {
		 		survive();
	 		} else if(toDoItem.left.equals("findFood")) {
	 			findFood();
	 		} else if(toDoItem.left.equals("findDrink")) {
	 			findDrink();
	 		} else if(toDoItem.left.equals("reference")) {
	 				
	 			//debug("about to return to reference... this should mean that we're done exploring");
	 			
	 			returnToUnique();
	 		} else if(toDoItem.left.equals("firstRoom")) {
	 			returnToReference();
	 		} else if(toDoItem.left.equals("explore")) {
	 			explore();
	 		} else if(toDoItem.left.equals("consider")) {
	 			consider(toDoItem);
	 		} else if(toDoItem.left.equals("getHere")) {
	 			getHere((Item)toDoItem.right);
	 		} else if(toDoItem.left.equals("sleep")) {
				sleep((Integer)toDoItem.right);
	 		} else if(toDoItem.left.equals("level")) {
	 			level(toDoItem.right);
	 		} else if(toDoItem.left.equals("kill")) {
	 			kill(((Being)toDoItem.right));
	 		} else if(toDoItem.left.equals("confirm")) {
	 			//referenceRoom.printCount();
	 			//currentRoom.printCount();
	 			//debug(" ~~~~~~~~~~~~~~~~~~~~~ just ENTERED confrim ~~~~~~~~~~~~~~~~~~~~~~");
	 			confirm();	 	
	 			
	 			//debug(" ~~~~~~~~~~~~~~~~~~~~~ just EXITED confrim ~~~~~~~~~~~~~~~~~~~~~~");
	 			//currentRoom.printTree();
	 			//referenceRoom.printCount();
	 			//currentRoom.printCount();
	 		} else if(toDoItem.left.equals("levelUpActions")) {
	 			levelUpActions();
	 		} else if(toDoItem.left.equals("reconsider")) {
	 			reConsider();
	 		} else {
	 			debug("Didn't know what to do with: " + toDoItem.left);
	 		}
	 	}
	 	//connection.send("quit");
	}
	
	private void kill(Being being) {
		debug("Killing------------");
		resultQueue.listen();
		connection.send("say Hey " + being.shortDesc + ", I'm about to kill you!");
		boredom = 0;
		int totalDamage = 0;
		int exp = experience;
		connection.send("kill " + being.name);
		//Fight happens here.
		String result;
		Pattern p = Pattern.compile("s you\\.\\[(\\d+) damage\\]");
		do{
			result = resultQueue.pop();
			Matcher m = p.matcher(result);
			if(result.startsWith("You KILLED")) {
				debug("We won the fight!");
				break;
			} else if (result.startsWith("You don't see")) {
				debug("The '" + being.name + "' left or were not here to start off with!");
				break;
			} else if (m.find()) {
				int damage = new Integer(m.group(1));
				//debug("They hurt me by " + damage);
				totalDamage += damage;
				
			} else {
				resultQueue.notMine(result);
			}
		} while(result != null);
		
		resultQueue.unlisten();
		exp = experience - exp;
		being.addFightData(totalDamage, exp);
		
		currentRoom.beings.remove(being);
		debug("-----------Done Killing");
	}

	private void reConsider() {
		debug("Reconsidering all of the mobiles because we leveled up.");
		for(int i = 0;i< knownBeingList.size();i++) {
			Being b = knownBeingList.get(i);
			for(Room r : b.seenIn) {
				walkToRoomIfKnown(r);
				if(currentRoom.beings.contains(b)) {
					if(reConsider(b)) {
						break;
					}
				}
			}
			
		}
	}
	private boolean reConsider(Being b) {
		resultQueue.listen();
		connection.send("consider " + b.name);
		String result;
		result = resultQueue.pop();
		while(!result.startsWith("You don't see") && !result.contains("looks much tougher than you") && !result.contains("looks about as tough as you") && !result.contains("You are much tougher")) {
			resultQueue.notMine(result);
			result = resultQueue.pop();
		}
		
		resultQueue.unlisten();
		return b.setGuessResult(result); 
		
	}
	private void levelUpActions() {
		toDoList.add(new Pair<String,Object>("reconsider", null));
		leveledRecently = false;
	}
	
	private void getHere(Item item) {
		
		
	}

	private void findDrink() {
		updateCarrying();
		thirsty = false;
	}

	private void findFood() {
		updateCarrying();
		hungry = false;
		
	}
	
	/**
	 * Uses the "inventory" command to update our knowledge of what we are carrying.
	 *
	 */
	private void updateCarrying() {
		resultQueue.listen();
		connection.send("inventory");
		String result = resultQueue.pop();
		while(result != null) {
			if(result.contains("Your Inventory:")) {
				carrying = new Vector<Item>();
				for(String line : result.split("\n")) {
					for(Item i : knownItemList) {
						if(i.getShort().equalsIgnoreCase(line)) {
							carrying.add(i);
						}
					}
				}
				result = null;
			} else {
				// Not what we are waiting for, pop!;
				resultQueue.notMine(result);
				result = resultQueue.pop();
			}
		}
		
		resultQueue.unlisten();
		debug("Updated carrying to contain: " + carrying);
	}

	/**
	 * Bisbat will do this when the goal is to gain experience, 
	 * with the ultimate goal of gaining in level.
	 * @param object 
	 *
	 */
	private void level(Object object) {

		// Only 2 phases, first phase we kill everything under the threshold once.
		// Second phase, we kill repeatedly the things that are the best exp.
		for(Being b : knownBeingList) {
			if(b.strength <= killThreshhold) {
				if(b.fights > 0) {
					//First iteration will skip this guy. 
					continue;
				}
				debug("We have seen " + b.shortDesc + b.seenIn);
				for(Room r : b.seenIn) {
					debug("Walking to the location of " + b.shortDesc);
					walkToRoomIfKnown(r);
					if(currentRoom.beings.contains(b)) {
						toDoList.add(new Pair<String,Object>("kill", b));
						return;
					}
				}
			}
				
		}
		debug("We have fought everyone within the threshold at least once.");
		try{
			Thread.sleep(20000);
		}catch(Exception e) {
			
		}
		double maxExpPerDamage = 0;
		Being best = null;
		for(Being b : knownBeingList) {
			if(b.averageDamage != 0 && (b.averageExperience/b.averageDamage > maxExpPerDamage)) {
				maxExpPerDamage = b.averageExperience/b.averageDamage;
				best = b;
				
			}
		}
		if(best == null) {
			// Should not happen, could happen if all beings did 0 damage. 
			//Or if there are no beings, 
			//or if we have never fought anyone.
			Thread.dumpStack();
			try{
				Thread.sleep(2000);
			} catch(Exception e) {
				
			}
		} else {
			for(Room r : best.seenIn) {
				debug("Walking to the location of " + best.shortDesc + " to kill them, because they give the best exp.");
				walkToRoomIfKnown(r);
				if(currentRoom.beings.contains(best)) {
					toDoList.add(new Pair<String,Object>("kill", best));
					return;
				}
			}
		}
		
			
		
	}

	

	public void login() {
		connection.send(name);
		connection.send(password);
		setUpPrompt();
		
		//We only need to do this if setUpPrompt() creates a non-prompt message from the game.
		//resultQueue.pop();
		
		roomFindingThread.add("look");
		currentRoom = roomFindingThread.pop();
		referenceRoom = currentRoom;
		referenceRoom.print();
		currentRoom.confirm(); // first room must be confirmed
	}
	
	public void explore() {	
		//debug("Starting exploration");
		String otherExitDirection = "";
		boolean foundOurWay = true;
		try {

			if(currentRoom.getRandomUnexploredExit() == null) {
				Room findMe = findRoomWithUnexploredExits();
				if(findMe == null) {
					print("We have finished mapping the known universe.");
					return;
				}
				toDoList.push(new Pair<String,Object>("explore",null));
				foundOurWay = walkToRoomIfKnown(findMe);
			} else {
				toDoList.push(new Pair<String,Object>("explore",null));
			}
			if (!foundOurWay) {
				return;
			}
			
			//we should now be at a room with an unexplored exit (if not, then there are non!)
			if(currentRoom.getRandomUnexploredExit() == null) {
				//debug("We are trying to explore, but there are no known unexplored exits");
				return;
			}
			
			Exit chosenExit = currentRoom.getRandomUnexploredExit();
			if (chosenExit == null) {
				debug("last update to chosenExit in bisbat.explore is really goofy");
			}
			follow(chosenExit);
			//debug("Exited follow at follow of bisbat");
			otherExitDirection = Exit.getOpposite(chosenExit.getDirection());

		} catch(NullPointerException e) {
			if(currentRoom == null) {
				Bisbat.debug("Current room was null.");
			} else {
				Bisbat.debug("Null Pointer\nCurrent Room Title: " + currentRoom.title + " otherExitDirection:" + otherExitDirection);
			}
			e.printStackTrace();
			toDoList.pop(); //bail so I can see what is happening
		} catch (Exception e) {
			Bisbat.debug("Error in random walk"); 
			e.printStackTrace();
		} 
	}
	
	/**
	 * Has Bisbat consider a being found in the game.
	 * @param toDoItem The toDoItem containing the being we are trying to see.
	 */
	private void consider(Pair<String,Object> toDoItem) {

		if(toDoItem.right instanceof Being) {
			Being b = ((Being)toDoItem.right);
			String name = b.guessName();
			if(name == null) {
				Bisbat.debug("The being is gone or has a really tricky name.");
				knownBeingList.remove(b);
				return;
			}
			resultQueue.listen();
			connection.send("consider " + name);
			//debug("Considering '" + name + "'.");
			String result = resultQueue.pop();
			
			// We don't want just any pop, we want a pop in response to our query.  Dumping all other input for now, later we will have to deal with these.
			while(!result.startsWith("You don't see") && !result.contains("looks much tougher than you") && !result.contains("looks about as tough as you") && !result.contains("You are much tougher")) {
				if(result.contains("leaves to the")) {
					debug("Someone left the room and now I am all fuddled.");
					knownBeingList.remove(b);
					return;
				}
				//debug("Dumping: " + result + " because it didn't match anything we were looking for.");
				resultQueue.notMine(result);
				result = resultQueue.pop();
			}
			
			resultQueue.unlisten();
				
			if(!b.setGuessResult(result)) {
				toDoList.push(new Pair<String,Object>("consider", b));
			} else {
				if(!b.isSureOfName()) {
					toDoList.push(new Pair<String,Object>("consider", b));
				} else {
					addReciprocol(b, currentRoom);
				}
			}
			
		} else {
			debug("Consider was added to the toDoList without a being");
		}
	}
	
	/**
	 * Instructs Bisbat to sleep in the game.
	 * @param duration: (seconds) length Bisbat should sleep.
	 */
	public void sleep(int duration) {
		connection.send("sleep");
		try{
			Thread.sleep(duration * 1000);
		} catch(Exception e) {
			e.printStackTrace();
		}
		connection.send("wake");
	}
	
	/**
	 * Instructs Bisbat to simply survive in the environment.
	 */
	public void survive() {
		toDoList.push(new Pair<String,Object>("survive", null));
		if(hungry) {
			toDoList.push(new Pair<String,Object>("findFood", null));
			return;
		}
		if(thirsty) {
			toDoList.push(new Pair<String,Object>("findDrink", null));
			return;
		}
		
		if(health < (maxHealth*.9) || mana < (.9*maxMana)) {
			heal();
			return;
		}
		if(leveledRecently == true) {
			toDoList.push(new Pair<String,Object>("levelUpActions",null));
			
			return;
		}
		if(boredom >= 0) {
			toDoList.push(new Pair<String,Object>("level",null));
			return;
		}
		if(move != maxMove) {
			heal();
			boredom++;
			return;
		}
		print("Good job, we have done everything we can in this game.");
		try{
			Thread.sleep(30000);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void heal() {
		debug("Sleeping to regain something. " + health + "/" + maxHealth + " " +
				 mana + "/" + maxMana + " " +
				 move + "/" + maxMove);
		toDoList.push(new Pair<String,Object>("sleep", 10));
		connection.send("save");
	}
	
	/**
	 * Instructions to determine if a room is unique. Finds all matching rooms, 
	 * consolidates if it is confirmed to be a previously found room. If something cannot 
	 * be confirmed, this method will add explore to the toDoList. If there is nothing to 
	 * confirm, this method will not add itself.
	 */
	public void confirm() {
		
		//currentRoom.print();
		//debug("This room has been confirmed: " + currentRoom.confirmed);
		//currentRoom.printTree();
		//find the nearest unconfirmed room, and go there
		if (currentRoom.confirmed()) {
			//debug("currentRoom was already confirmed");
			ArrayList<Exit> path = RoomFinder.searchForPathToUnconfirmedRoom(currentRoom);
			if (path == null || path.isEmpty()) {
				//we didn't find an unconfirmed room in the knowledge base, we're done here
				//debug("exiting confirm at exit 1");
				//debug("Couldn't find any unconfirmed rooms");
				return;
			}
			for(Exit exit : path) {
				follow(exit);
				if (interrupted) {
					// we have been interrupted, try again.
					toDoList.push(new Pair<String,Object>("confirm",null));
					debug("exiting confirm from being interrupted");
					return;
				} 
			}
		}
		
		//debug("Trying to Confirm this room 1:");
		//currentRoom.printTree();
		//currentRoom.printCount();
		
		//toDoList.push(new Pair<String,Object>("reference",null));
		toDoList.push(new Pair<String,Object>("confirm",null));
		
		//we are now at the nearest unconfirmed room it is likely that we didn't move.
		//store this room, it wil be usefull if we end up consolidating!
		Room previous = currentRoom;		
		
		//find all the (confirmed) rooms that match this room in the knowledge base.
		LinkedList<Room> matches = RoomFinder.searchForAllMatchingRooms(referenceRoom, currentRoom);
		LinkedList<Room> temp = new LinkedList<Room>();
		
		//remove the currentRoom from matches.
		matches.remove(currentRoom);
		
		for (Room room : matches) {
			if (!room.confirmed()) {
				temp.add(room);
			}
		}
		//store these rooms, if we consolidate, one of these will be the one consolidated
		LinkedList<Room> matched = new LinkedList<Room>(matches);
		//remove all unconfirmed rooms from matches
		matches.removeAll(temp);
		temp.clear();
		
		if (matches.isEmpty()){
			//no confirmed rooms match this room. This is a new room, confirm.
			previous.confirm();
			currentRoom = previous;

			debug("Confirming a room based on zero matching rooms.");
		} else {
			/* There is at least one confirmed room that matches our current room.
			 * Since all of these rooms have been confirmed, there must be a known path,
			 * i.e. a path of confirmed exits, that distinguished them from eachother.
			 * if we follow this path, we will be able to determine which, if any of those
			 * rooms, our currentRoom matches. */
			
			LinkedList<String> dirPath = RoomFinder.commonConfirmedPath(matches);
			LinkedList<String> followedPath = new LinkedList<String>();
			
			int counter = 0; // how many of these have we taken?
			int countDown = 4; // travel only 4 exits after reducing to only one matching room.
			
			//disconnect any previous attachment to the rest of the knowledge base
			String connectingDirection = null;
			Room connectingRoom = null;
			for(Exit e : currentRoom.exits) {
				if (e.nextRoom != null) {
					connectingDirection = e.getDirection();
					connectingRoom = e.nextRoom;
					e.nextRoom = null;
				}
			}
			
			//debug("The current Room!");
			//debug("dirPath has size: " + dirPath.size());
			for(String dir : dirPath) {
				if (countDown == 0) {
					break;
				}
				for (Room room : matches) {
					if (room != null && room.getExit(dir) != null) {
						temp.add(room.getExit(dir).nextRoom);
					}
				}
				matches.clear();
				matches.addAll(temp);
				temp.clear();
				//follow our direction, see what we get
				//debug("Is this the follow command that yields the trying to follow(null) message");
				follow(currentRoom.getExit(dir), false);
				
				followedPath.addLast(dir); //path we are following.
				counter++;
				if (matches.size() <= 1) {
					countDown--;
				}
				if (currentRoom == null) {
					debug("Well... our currentRoom is null... ?");
				}
				for (Room room : matches) {
					if (!currentRoom.matchesRoom(room)) {
						temp.add(room);
					}
				}
				//remove everything that no longer matches what we see
				matches.removeAll(temp);
				if(matches.isEmpty()) {
					break;
				}
			}
			//debug("We should have just finished following our distinguishing path.");
			//there are three cases here
			if (matches.isEmpty()){
				//previous is a unique room, confirm it's existence
				//debug("we have determined that this is a unique room!");
				//debug("Confirming a room based on zero matching rooms after path exploration.");
				
				/* In this case, we have followed what we knew was a connected 
				 * path and ended up someplace other than we hypothesized we 
				 * would. This means that not only was the hypothetical room 
				 * unique, but so were the rooms along the path. (This is not
				 * always true, but will be used as an assumption.)
				 */
				
				//reconnect new rooms to the knowledge base
				if (connectingDirection != null){
					previous.getExit(connectingDirection).nextRoom =  connectingRoom;
				}
				
				for(String dir : followedPath) {
					
					previous.confirm();
					if (previous.getExit(dir) != null && previous.getExit(dir).nextRoom != null) {
						previous = previous.getExit(dir).nextRoom;
					}
					
					//debug("Out of the loop.");
				}
				if (previous != null) {
					previous.confirm();
					currentRoom = previous;
				} else {
					debug("Previous was null, FIX THIS!");
				}
				
				//currentRoom.printTree();
				
			} else if (matches.size() == 1) {
				//debug("Trying to consolidate two rooms:");
				//previous is (to a depth of 10) the same as the one element in matches
				//which original room went to this room.
				Room toConsolidate = null;
				for(Room room : matched) {
					if (currentRoom.matchesRoom(room.roomAfterPath(followedPath))) {
						toConsolidate = room;
						break;
					}
				}
				//debug("attempting to consolidate");	
				if (toConsolidate != null){
					//consolidate rooms.
					
					/*
					debug("Printing the tree from current (disconnected location)");
					currentRoom.printTree();
					debug("Printing the tree from previous (disconnected location)");
					previous.printTree();
					debug("Printing the tree from toConsolidate (disconnected location)");
					toConsolidate.printTree();
					*/
					
					ArrayList<Exit> exits = RoomFinder.searchForPathBetweenRooms(toConsolidate, previous);
					LinkedList<String> path = new LinkedList<String>();
					for(Exit e : exits) {
						path.addLast(e.getDirection());
					}
					String last = path.removeLast();
				    Room before = toConsolidate.roomAfterPath(path);
				    before.getExit(last).nextRoom = toConsolidate;
					
					//previous = toConsolidate;
					currentRoom = toConsolidate.roomAfterPath(followedPath);
					
					
					//debug("Finished Consolidation");
				} else {
					// If anything goes wrong here, we need to know it.
					// This should not happen ever. - Brian (And it was happening a lot)
					debug("Something went wrong while consolidating inside confirm()");
					debug("Size of list of matching rooms: " + matches.size());
					previous.print();
					currentRoom.print();
					matches.getFirst().print();
					debug("__________________________________________________");
					for(Room room : matched) {
						room.print();
					}
				}
			} else {
				//something went wrong, coudn't distinguish by distinguishing path
				debug("Couldn't confirm a room with a distinguishing path of length 10");
				previous.print();
			}
			//debug("exiting confirm at exit 3");
			
		}
	}
	
	
	/**
	 * Searches the knowledge base for an unexplored exit.
	 * Search opperates breadth-first from Bisbat's current location.
	 * @return: first room that is found with an unexplored exit
	 */
	public Room findRoomWithUnexploredExits() {
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		searchQueue.add(currentRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				if(currentRoom.getUnexploredExits().size() > 0) {
					return currentRoom;
				} else {
					for(Exit e : currentRoom.exits) {
						if(e.nextRoom != null) {
							searchQueue.add(e.nextRoom);
						}
					}
				}
			}	
		}
		return null; // no unexplored exits found in knowledge base
	}
	
	
	public String getPrompt() {
		return prompt;
	}
	public String getPromptMatch() {
		String s = getPrompt().replaceAll("%c", ".?.?");
		s = s.replaceAll("%.", "(\\\\d+)");
		s = s.replaceAll("&.", "");
		return s;
	}
	public void setUpPrompt() {
		prompt = "&W<prompt %h/%H %m/%M %v/%V %x>%c";
		connection.send("prompt " + prompt);
		
	}
	public void updateWithPrompt(String promptString) {
		Pattern p = Pattern.compile(getPromptMatch());
		Matcher m = p.matcher(promptString);
		if(!m.matches()) {
			debug("Having trouble matching " + promptString + " and " + getPromptMatch());
		} else {
			//debug("Should be fine.");
		}
		
		health = new Integer(m.group(1));
		maxHealth = new Integer(m.group(2));
		mana = new Integer(m.group(3));
		maxMana = new Integer(m.group(4));
		move = new Integer(m.group(5));
		maxMove = new Integer(m.group(6));
		experience = new Integer(m.group(7));
	}

	public void foundRoom(Room recentlyDiscoveredRoom) {
		roomFindingThread.add(recentlyDiscoveredRoom);
	}
	
	/**
	 * This method will be used to walk to a room that has unexplored exits, or
	 * whenever we want to walk to a destination room.
	 * @param walkMeToHere: room Bisbat should walk to.
	 * @return: True if we walked all the way to given room, false otherwise.
	 */
	public boolean walkToRoomIfKnown(Room walkMeToHere) {
		ArrayList<Exit> path = null;
		try {
			path = RoomFinder.searchForPathBetweenRooms(currentRoom, walkMeToHere);
			//debug("Path is " + path.size() + " long.");
		} catch(OutOfMemoryError e) {
			System.out.println("Path betwen rooms -> out of memory!");
		}
		if(path == null) {
			throw new NullPointerException("Couldn't find a path to desired room.");
		}
		for(Exit e : path) {
			follow(e); //follows given exit.
			if (interrupted) {
				return false; //failed to get to where we wanted to go.
			} 
		}
		return true; // done following the path
	}
	
	
	
	/**
	 * instructs bisbat to return to our refernce room.
	 * If he find his way this will add explore to the todoList.
	 * not just a uniquely idnetified room.
	 * Returns true if it failed
	 */
	public boolean returnToReference(){
		return returnTo(true);
	}
	
	public boolean returnToUnique(){
		return returnTo(false);
	}
	
	private boolean returnTo (boolean toFirstRoom){
		ArrayList<Exit> path = new ArrayList<Exit>();
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		searchQueue.add(currentRoom);
		
		if (toFirstRoom) {
			path = RoomFinder.searchForPathBetweenRooms(currentRoom, referenceRoom);
		} else {
			while(searchQueue.size() > 0) {
				Room temp = searchQueue.removeFirst();
				LinkedList<Room> rooms = RoomFinder.searchForAllMatchingRooms(referenceRoom,temp);
				if(!exploredRooms.contains(temp)) {
					exploredRooms.add(temp);
				} else {
					continue;
				}
				if(rooms.size() == 1 && rooms.getFirst().confirmed()) {
					path = RoomFinder.searchForPathBetweenRooms(currentRoom, temp);
					break;
				} else {
					for(Exit e : temp.exits) {
						if(e.nextRoom != null) {
							searchQueue.add(e.nextRoom);
						}
					}
				}
			}
		}
		if(path == null) {
			toDoList.push(new Pair<String,Object>("explore",null));
			return false;
		} else {
			for(Exit e : path) {
				follow(e); //follows given exit.
				if (interrupted) {
					toDoList.push(new Pair<String,Object>("explore",null));
					return false; //failed to get to where we wanted to go.
				} 
			}
			connection.send("save");
			return true;
		}
	}
	
	
	/**
	 * This method is called for instructing bisbat to follow a given exit.
	 * In the process of following this exit, several things occur:
	 * 1. Determines if this exit goes where bisbat thinks it should.
	 * 2. Determines if next room is a room bisbat has already been to.
	 * 3. Ensures that knowledge of rooms is not lost.
	 * 4. Finds mazes, and other anomalies.
	 * @param chosenExit: exit to follow
	 * @return: true if we went to a known room, otherwise, false;
	 */
	public boolean follow(Exit chosenExit) {
		return follow(chosenExit, true);
	}
	
	/**
	 * This method is called for instructing bisbat to follow a given exit.
	 * In the process of following this exit, several things occur:
	 * 1. Determines if this exit goes where bisbat thinks it should.
	 * 2. Determines if next room is a room bisbat has already been to.
	 * 3. Ensures that knowledge of rooms is not lost.
	 * 4. Finds mazes, and other anomalies.
	 * @param chosenExit: exit to follow
	 * @param exploring: False only in the case that we are not looking for new rooms.
	 * @return: true if we went to a known room, otherwise, false;
	 */
	public boolean follow(Exit chosenExit, boolean exploring) {

		if (chosenExit == null) {
			//Bisbat.debug("we are trying to follow(null) and it isn't working");
			return false; 
		}
		
		chosenExit.confirm();
		Room previousRoom = currentRoom; //store the previous room
		if(chosenExit.getDoorCommand() != null) {
			connection.send(chosenExit.getDoorCommand()); //open a door if it's there
		}
		
		//debug("--> " + chosenExit.direction);
		
		connection.sendNavigation(chosenExit.direction);
		Room result = roomFindingThread.pop();
		if(result != null) {
			currentRoom = result;
		} else {
			interrupted = true;
			return false;
			
		}
		//did we go where we expected to go?
		if (!exploring) {
			//debug("Recognize that we are NOT exploring");
			//currentRoom.printTree();
			if (chosenExit.nextRoom != null && chosenExit.nextRoom.matchesRoom(currentRoom)){
				//path went where it was expected to go.
				//chosenExit.nextRoom.update(currentRoom);
				chosenExit.nextRoom.update(currentRoom);
				currentRoom = chosenExit.nextRoom;
				//possibility of bidriectional exit?
				if(currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())) != null) {
					//there is the possibilty of a bidirectional exit (assume that it is one)
					currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())).nextRoom = previousRoom;
				}
				return true;
			} else {
				chosenExit.nextRoom = currentRoom;
				//possibility of bidriectional exit?
				if(currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())) != null) {
					//there is the possibilty of a bidirectional exit (assume that it is one)
					currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())).nextRoom = previousRoom;
				}
				return false;
			}
		} else if (chosenExit.nextRoom != null && chosenExit.confirmed && chosenExit.nextRoom.matchesRoom(currentRoom)) {
			//path went where it was expected to go.
			//chosenExit.nextRoom.update(currentRoom);
			chosenExit.nextRoom.update(currentRoom);
			currentRoom = chosenExit.nextRoom;
			return true;
		} else {
			//path did not go where we expected or we didn't know where it was going
			//note that there is no longer a path from currentRoom to the referenceRoom
			//does this room match the room we just came from... if so, acting assumption that it's a new room.
			if (currentRoom.matchesRoom(previousRoom)) {
				currentRoom.confirm();
				//this is a new room, is it possible that it's bidirectional. If so, assume it is.
				if(currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())) != null) {
					//there is the possibilty of a bidirectional exit (assume that it is one)
					currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())).nextRoom = previousRoom;
				}
				//connect previous room to our current room.
				chosenExit.nextRoom = currentRoom;
				return false;
			}
			
			//is this a room that we have already been too?
			chosenExit.nextRoom = null; // ensure that we're not acting on incorrect knowledge base
			LinkedList<Room> matches = RoomFinder.searchForAllMatchingRooms(referenceRoom, currentRoom);
			chosenExit.nextRoom = currentRoom;
			
			if (matches.isEmpty()) {
				//this is not a room we have been in before (integrate new room into the knowledge base)
				//confirm this room as existing... since it has no matches
				currentRoom.confirm();
				//is there the possibility of a bidirectional exit
				if(currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())) != null) {
					//there is the possibilty of a bidirectional exit (assume that it is one)
					currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())).nextRoom = previousRoom;
					return false;
				} else {
					//do nothing, we don't know how to get back to referenceRoom yet
					//cannot get back to the reference room, should probably explore
					debug("how often is this case of follow happening?");
					return false;
				}				
			} else {
				//room matches at least 1 room in knowledge base
				//is there the possibility of a bidirecitonal exit?
				if(currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())) != null) {
					//there is the possibilty of a bidirectional exit (assume that it is one)
					currentRoom.getExit(Exit.getOpposite(chosenExit.getDirection())).nextRoom = previousRoom;
				}
				//interrupt current toDoItem and confirm room!
				interrupted = true;
				toDoList.push(new Pair<String,Object>("confirm",null));
				return false;
			}
		}
	}
	
	public void addKnowledgeOf(Item i) {
		if(i == null) {
			debug("You are trying to add knowledge of a null, this is a problem.");
			return;
		}
		if(!knownItemList.contains(i)) {
			knownItemList.add(i);
			toDoList.push(new Pair<String,Object>("getHere", i));
		}
	}
	
	public Being addKnowledgeOf(Being b) {
		if(b == null) {
			debug("You are trying to add knowledge of a null, this is a problem.");
		}
		if(!knownBeingList.contains(b)) {
			knownBeingList.add(b);
			toDoList.push(new Pair<String,Object>("consider", b));
			return b;
		} else {
			return knownBeingList.get(knownBeingList.indexOf(b));
		}
	}

	private void addReciprocol(Being b, Room r) {
		if(!b.seenIn.contains(r)) {
			b.seenIn.add(r);
		}
		if(!r.beings.contains(b)) {
			r.beings.add(b);
		}
	}
	/**
	 * Prints the time with given string
	 * @param string: prints this string along with the current time
	 */
    static public void print(String string) {
    	cal = new GregorianCalendar();
        System.out.println("(" + cal.getTime().toString() + ") "  + string);
    }
    
    static public void debug(String string) {
    	System.out.println(string);
    }
	
	/**
	 * Creates and runs default instance of Bisbat
	 * @param args
	 */
	public static void main(String[] args) {
		Bisbat alpha = new Bisbat("cisbat", "alpha");
		alpha.start(); 
	}
	
	
}
