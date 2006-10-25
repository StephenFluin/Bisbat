package bisbat;

import java.util.Vector;

public class Being {
	
	public String name, shortDesc, longDesc;
	public int hp, maxhp;
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
	
	public Being(String string) {
		longDesc = string;
	}
	public boolean equals(Being other) {
		Bisbat.debug("checking to see if mob is equal to another.");
		return longDesc.equalsIgnoreCase(other.longDesc);
	}
	
	public boolean isSureOfName() {
		return (name!=null && name.length() > 2);
	}
	
	public String guessName() {
		String[] names = longDesc.split(" ");
		if(guessMode == 0) {
			return names[guessLocation].substring(0,1);
		} else {
			if(guessLocation >= names.length) {
				Bisbat.debug("We couldn't figure out the name of the mobile.  There is a chance she left us.");
			}
			return names[guessLocation];
		}
	}
	public boolean setGuessResult(String result) {
		if(result.startsWith("You don't see that here.")) {
			setGuessResult(false);
			return false;
		} else {
			setGuessResult(true);
			return true;
		}
	}
	public void setGuessResult(boolean sucess) {
		if(sucess) {
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

}
