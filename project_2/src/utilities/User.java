package utilities;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class User implements Serializable {
//	public static final String DIV_EMERGENCY = "div_emergency";
//	public static final String DIV_ACTIVECARE = "div_activecare";
//	public static final String DIV_REHAB = "div_rehab";

	private String username;
	private Division division;
	private String certNbr;
	protected PermissionLevel permLevel;
	private ArrayList<Record> records;

	public User(String username, Division division, String certNbr) {
		this.username = username;
		this.division = division;
		this.certNbr = certNbr;
		this.records = new ArrayList<Record>();
	}

	public String getUsername() {
		return username;
	}

	public String getCertNbr() {
		return certNbr;
	}

	public Division getDivision() {
		return division;
	}

	public PermissionLevel getPermissions() {
		return permLevel;
	}

	public void addRecord(Record r) {
		records.add(r);
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
		if (r == null)
			return false;
		
		return r.read(this);
	}

	public boolean writeRecord(Record r) {
		return r.write(this);
	}
	
	public boolean deleteRecord(Record r) {
		return r.delete(this);
	}

	public boolean createRecord() {
		if (permLevel == PermissionLevel.Doctor) {
			return true;
		}

		return false;
	}
	
	public void createRecord(Doctor doctor, Nurse nurse, Patient patient, Division divsion, String medicalData) {
		
		Record r = new Record(doctor, nurse, patient, division, medicalData);
		Database db = Database.getInstance();
		db.insertRecord(r);
		db.updateRecords();
		this.addRecord(r);
		
	}

	public User login(String recvCertNbr) {
		if (recvCertNbr.equals(certNbr)) {
			return this;
		} else {
			return null;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(username);
		sb.append("\t");
		sb.append(division.getName());
		sb.append("\t");
		sb.append(certNbr);
		sb.append("\t");
		sb.append(permLevel);

		return sb.toString();
	}
}
