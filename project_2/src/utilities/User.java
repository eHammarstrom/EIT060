package utilities;

import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;

public abstract class User {
	private String username;
	private String password;
	private int certNbr;
	protected PermissionLevel permLevel;
	private ArrayList<Record> records;
	
	public User(String username, String password, int certNbr) {
		this.username = username;
		this.password = BCrypt.hashpw(password, BCrypt.gensalt());
		this.certNbr = certNbr;
		this.records = new ArrayList<Record>();
	}
	
	public void addRecord(Record r) {
		records.add(r);
	}
	
	public void loadUser() {
		// Here we call the class which loads the json user file.
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(username);
		sb.append("\n");
		sb.append("\t");
		sb.append(password);
		sb.append("\n");
		sb.append("\t");
		sb.append(certNbr);
		sb.append("\n");
		sb.append("\t");
		sb.append(permLevel);
		sb.append("\n");
		if (records != null) {
			for (Record r : records) {
				sb.append("\t" + r.toString());
			}
		}
		
		return sb.toString();
	}
}
