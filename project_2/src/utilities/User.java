package utilities;

import java.io.Serializable;
import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;

public abstract class User implements Serializable {
	public static final String DIV_EMERGENCY = "div_emergency";
	public static final String DIV_ACTIVECARE = "div_activecare";
	public static final String DIV_REHAB = "div_rehab";

	private String username;
	private String password;
	private String division;
	private long certNbr;
	protected PermissionLevel permLevel;
	private ArrayList<Record> records;
	
	public User(String username, String password, String division, long certNbr) {
		this.username = username;
		this.password = BCrypt.hashpw(password, BCrypt.gensalt());
		this.division = division;
		this.certNbr = certNbr;
		this.records = new ArrayList<Record>();
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public long getCertNbr() {
		return certNbr;
	}
	
	public String getDivision() {
		return division;
	}
	
	public PermissionLevel getPermissions() {
		return permLevel;
	}
	
	public void addRecord(Record r) {
		records.add(r);
	}
	
	public User login(String username, String password) {
		if (username.equals(this.username) && BCrypt.checkpw(password, this.password)) {
			System.out.println("Matched login.");
			return this;
		} else {
			System.out.println("Login error.");
			return null;
		}
	}
	
	public void loadUser() {
		// Here we call the class which loads the json user file.
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(username);
	//	sb.append("\n");
		sb.append("\t");
	//	sb.append(password);
	//	sb.append("\n");
	//	sb.append("\t");
		sb.append(division);
	//	sb.append("\n");
		sb.append("\t");
		sb.append(certNbr);
	//	sb.append("\n");
		sb.append("\t");
		sb.append(permLevel);
	//	sb.append("\n");
		if (records != null) {
			for (Record r : records) {
				sb.append("\t" + r.toString());
			}
		}
		
		return sb.toString();
	}
}
