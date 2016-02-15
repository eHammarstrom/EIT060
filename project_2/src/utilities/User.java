package utilities;

import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;

public abstract class User {
	private String username;
	private String password;
	private int certNbr;
	private PermissionLevel permLevel;
	private ArrayList<Record> records;
	
	public User(String username, String password, int certNbr) {
		this.username = username;
		this.password = BCrypt.hashpw(password, BCrypt.gensalt(2));
		this.certNbr = certNbr;
	}
	
	public void loadUser() {
		// Here we call the class which loads the json user file.
	}
}
