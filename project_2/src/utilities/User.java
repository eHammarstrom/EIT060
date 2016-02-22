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
	private String certNbr;
	protected PermissionLevel permLevel;
	private ArrayList<Record> records;

	public User(String username, String password, String division, String certNbr, boolean readMode) {
		this.username = username;

		if (readMode)
			this.password = password;
		else
			this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));

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

	public String getCertNbr() {
		return certNbr;
	}

	public String getDivision() {
		return division;
	}

	public PermissionLevel getPermissions() {
		return permLevel;
	}

	public boolean isAssociated(Record r) {
		for (Record tempRecord : records) {
			if (tempRecord.getId() == r.getId()) {
				return true;
			}
		}

		return false;
	}

	public boolean readRecord(Record r) {
		return r.read(this);
	}

	public boolean writeRecord(Record r) {
		return r.write(this);
	}

	public boolean createRecord() {
		if (permLevel == PermissionLevel.Doctor) {
			return true;
		}

		return false;
	}

	public boolean deleteRecord(Record r) {
		return r.delete(this);
	}

	public void addRecord(Record r) {
		records.add(r);
	}

	public User login(String recvUsername, String recvPassword, String recvCertNbr) {
		if (recvUsername.equals(username) && BCrypt.checkpw(recvPassword, password) && recvCertNbr.equals(certNbr)) {
			return this;
		} else {
			return null;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(username);
		sb.append("\t");
		sb.append(division);
		sb.append("\t");
		sb.append(certNbr);
		sb.append("\t");
		sb.append(permLevel);

		if (records != null) {
			for (Record r : records) {
				sb.append("\t" + r.toString());
			}
		}

		return sb.toString();
	}
}
