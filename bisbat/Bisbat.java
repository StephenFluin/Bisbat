package bisbat;

public class Bisbat {

	public static Connection c;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		 c = new Connection("www.mortalpowers.com", 4000);
		 login();
	}
	public static void login() {
		c.send("Bisbat\nalpha\n");
	}

}
