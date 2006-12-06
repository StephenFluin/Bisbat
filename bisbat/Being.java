package bisbat;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Being {
	
	public String name, shortDesc, longDesc;
	public int hp, maxhp;
	
	/**
	 * Contains a list of total damages dealt by this mobile for 
	 * each fight.
	 */
	public Vector<Integer> damageHistory;
	public Vector<Room> seenIn;
	public int guessLocation = 0;
	
	/**
	 * Four modes:
	 * 0. single character guesses
	 * 1. entire word guesses
	 * 2. guesses from room title
	 * 3. guesses from room description (unimplmeented yet)
	 */
	public int guessMode = 0;
	
	/**
	 * 0 = unknown
	 * 1 = Less tough than you
	 * 2 = Same as you
	 * 3 = Tougher than you.
	 */
	public int toughness = 0;
	
	/**
	 * 0 = unknown
	 * 1 = Couldn't hurt you
	 * 2 = Medium
	 * 3 = Could hurt you a lot
	 */
	public int strength = 0;
	
	
	public Being(String string) {
		longDesc = string;
	}
	public boolean equals(Being other) {
		//Bisbat.debug("checking to see if mob is equal to another.");
		return longDesc.equalsIgnoreCase(other.longDesc);
	}
	public boolean equals(Object other) {
		if(other instanceof Being) {
			return equals((Being)other);
		} else {
			return false;
		}
	}
	public boolean isSureOfName() {
		return (name!=null && name.length() > 2);
	}
	
	public String guessName() {
		String[] names = longDesc.split(" ");
		if(guessMode == 0) {
			if(guessLocation >= names.length) {
				Bisbat.debug("We couldn't figure out the name of the mobile.  There is a chance she left us or her long is misleading");
				return null;
			}
			return names[guessLocation].substring(0,1);
		} else {
			if(guessLocation >= names.length) {
				Bisbat.debug("We couldn't figure out the name of the mobile.  There is a chance she left us.");
				return null;
			}
			return names[guessLocation];
		}
	}
	public boolean setGuessResult(String result) {
		if(result.startsWith("You don't see that here.")) {
			setGuessResult(false);
			return false;
		} else {
			Pattern p = Pattern.compile("(.*) looks much tougher than you\\..*", Pattern.MULTILINE | Pattern.DOTALL);
			Pattern p2 = Pattern.compile("You are much tougher than (.*?)\\..*", Pattern.MULTILINE | Pattern.DOTALL);
			Pattern p3 = Pattern.compile("(.*) looks about as tough as you\\..*", Pattern.MULTILINE | Pattern.DOTALL);
			//'<--cisbat looks about as tough as you.
			//cisbat could hurt you a fair amount.'
			//Neither thing matched
			Matcher m = p.matcher(result);
			Matcher m2 = p2.matcher(result);
			Matcher m3 = p3.matcher(result);
			if(m.matches()) {
				//Bisbat.debug("Someone looks much tougher than us, lets grab '" + m.group(1) + "'.");
				shortDesc = m.group(1);
				name = stripOfPronouns(shortDesc);
				toughness = 3;
			} else if(m3.matches()) {
				shortDesc = m3.group(1);
				name = stripOfPronouns(shortDesc);
				toughness = 2;
				
			} else if(m2.matches()) {
				shortDesc = m2.group(1);
				name = stripOfPronouns(shortDesc);
				toughness = 1;
			} else {
				Bisbat.debug("Nothing matched");
				return false;
			}
			// \n\r?
			p = Pattern.compile(".*(.*) looks like he could hurt you a lot\\.", Pattern.MULTILINE | Pattern.DOTALL);
			p2 = Pattern.compile(".*(.*) could hurt you a fair amount\\.", Pattern.MULTILINE | Pattern.DOTALL);
			p3 = Pattern.compile(".*(.*) probably couldn't hurt you very much\\.", Pattern.MULTILINE | Pattern.DOTALL);
			m = p.matcher(result);
			m2 = p2.matcher(result);
			m3 = p3.matcher(result);
			
			if(m.matches()) {
				Bisbat.debug("Really strong.");
				strength = 3;
			} else if (m2.matches()) {
				Bisbat.debug("kinda strong.");
				strength = 2;
			} else if(m3.matches()) {
				Bisbat.debug("kinda weak.");
				strength = 1;
			}
			setGuessResult(true);
			return true;
		}
	}
	public void setGuessResult(boolean success) {
		if(success) {
			if(guessMode == 0) {
				guessMode = 1;
			} else if(guessMode == 1) {
				name = longDesc.split(" ")[guessLocation];
				if(name.length() < 4) {
					guessMode = 0;
					guessLocation++;
				}
			}
		} else {
			if(guessMode == 0) {
				guessLocation++;
				if(guessLocation >= longDesc.length()) {
					guessMode = 2;
				}
			} else if(guessMode == 1) {
				guessLocation++;
				if(guessLocation >= longDesc.length()) {
					guessMode = 2;
				}
			}
		}
	}

	public static String stripOfPronouns(String string) {
		String result = string.replaceAll("[tT]he ", "");
		result = result.replaceAll("[Aa] ", "");
		result = result.replaceAll("[Aa]n ", "");
		return result;
	}
	
	public String toString() {
		return (shortDesc == null ? longDesc : shortDesc) + " (" + strength + "," + toughness + ") -" + longDesc;
	}
}
