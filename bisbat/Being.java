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
		return longDesc.equalsIgnoreCase(other.longDesc);
	}
	
	public boolean isSureOfName() {
		return (name!=null && name.length() > 3);
	}
	
	public String guessName() {
		String[] names = longDesc.split(" ");
		if(guessMode == 0) {
			return names[guessLocation].substring(0,1);
		} else {
			return names[guessLocation];
		}
	}
	
	public void setGuessResult(boolean sucess) {
		if(sucess) {
			if(guessMode == 0) {
				guessMode = 1;
			} else if(guessMode == 1) {
				name = longDesc.split(" ")[guessLocation];
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
