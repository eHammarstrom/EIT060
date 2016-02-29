package utilities;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class User implements Serializable {
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
	
	/***
	 * Tries to login the user with a specific certNbr
	 * @param recvCertNbr
	 * @return this user or null
	 */
	
	public User login(String recvCertNbr) {
		if (recvCertNbr.equals(certNbr)) {
			return this;
		} else {
			return null;
		}
	}
	
	/***
	 * Checks if this user is associated with a certain record r
	 * @param r
	 * @return true or false
	 */
	
	public boolean isAssociated(Record r) {
		for (Record tempRecord : records) {
			if (tempRecord.getId() == r.getId()) {
				return true;
			}
		}

		return false;
	} 
	
	/***
	 * Adds a record to the user
	 * @param r
	 */

	public void addRecord(Record r) {
		records.add(r);
	}
	
	/***
	 * Checks if this user can read a certain record
	 * @param r
	 * @return true or false
	 */

	public boolean readRecord(Record r) {
		if(r == null) {
		 	return false;
		}
		return r.read(this);
	}
	
	/***
	 * Checks if this user has permission to write to a certain record
	 * @param r
	 * @return true or false
	 */

	public boolean writeRecord(Record r) {
		if(r == null) {
			return false;
		}
		return r.write(this);
	}
	
	/***
	 * Checks if this user has permission to delete a certain record
	 * @param r
	 * @return true or false
	 */
	
	public boolean deleteRecord(Record r) {
		if(r == null) {
			return false;
		}
		return r.delete(this);
	}
	
	/***
	 * Checks if this user has permission to create a new record
	 * @return true or false
	 */

	public boolean createRecord() {
		if (permLevel == PermissionLevel.Doctor) {
			return true;
		}
		return false;
	}
	
	/***
	 * Creates a new record and updates the database
	 * @param doctor, nurse, patient, division. medicalData
	 */
	
	public void createRecord(Doctor doctor, Nurse nurse, Patient patient, Division divsion, String medicalData) {
		
		Record r = new Record(doctor, nurse, patient, division, medicalData);
		Database db = Database.getInstance();
		db.insertRecord(r);
		db.updateRecords();
		this.addRecord(r);
		
	}
	
	/***
	 * Makes a appropriate String description for the log
	 * @return String
	 */

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
