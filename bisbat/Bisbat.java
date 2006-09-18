package bisbat;

public class Bisbat extends Thread {

	public Connection c;
	public String name = "Bisbat";
	public String password = "alpha";
	private String prompt;
	public Bisbat() {
		prompt = "";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Bisbat alpha = new Bisbat();
		alpha.start();
		 
	}
	public void run() {
		c = new Connection(this, "www.mortalpowers.com", 4000);
	 	login();
	 	explore();
	}
	public void login() {
		c.send(name);
		c.send(password);
		setUpPrompt();
	}
	public void explore() {
		c.send("look");
	}
	public void setUpPrompt() {
		prompt = "<prompt>%c";
		c.send("prompt " + prompt);
		
	}
	public String getPrompt() {
		return prompt;
	}
	public String getPromptMatch() {
		return ".?" + getPrompt().replaceAll("%.", ".?.?");
	}


}
