package utilities;

import java.io.Serializable;

public class Record implements Serializable {
	private Doctor doctor;
	private Nurse nurse;
	private Patient patient;
	private Division division;
	private String medicalData;
	private long id;

	public Record(Doctor doctor, Nurse nurse, Patient patient, Division division, String medicalData) {
		this.doctor = doctor;
		this.nurse = nurse;
		this.patient = patient;
		this.division = division;
		this.medicalData = medicalData;

		doctor.addRecord(this);
		nurse.addRecord(this);
		patient.addRecord(this);
	}
	
	public void setRecordId(long id) {
		this.id = id;
	}

	public String getDoctorCertNbr() {
		return doctor.getCertNbr();
	}

	public String getNurseCertNbr() {
		return nurse.getCertNbr();
	}

	public String getPatientCertNbr() {
		return patient.getCertNbr();
	}

	public Division getDivision() {
		return division;
	}

	public String getMedicalData() {
		return medicalData;
	}

	public long getId() {
		return id;
	}

	public String toString() {
		return Long.toString(id);
	}
	
	/***
	 * Checks if the user has permission to read a certain record 
	 * @return true or false
	 */

	public boolean read(User user) {
		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Doctor)
				&& (user.getDivision().equals(division) || user.getCertNbr().equals(doctor.getCertNbr()))) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Nurse)
				&& (user.getDivision().equals(division) || user.getCertNbr().equals(nurse.getCertNbr()))) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Patient)
				&& user.getCertNbr().equals(patient.getCertNbr())) {
			return true;
		}
			
		return false;
	}
	
	/***
	 * Checks if the user has permission to write to certain record 
	 * @return true or false
	 */

	public boolean write(User user) {
		if (user.getPermissions().equals(PermissionLevel.Doctor) && user.getDivision().equals(doctor.getDivision())) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Nurse) && user.getCertNbr() == nurse.getCertNbr()) {
			return true;
		}

		return false;
	}
	
	/***
	 * Writes to a specific record with new medical data 
	 * @param newData
	 */

	public void write(String newData) {
		medicalData = newData;
		Database db = Database.getInstance();
		db.writeRecord(newData, id);
		db.updateRecords();
	}
	
	/***
	 * Checks if the user has permission to delete a certain record 
	 * @return true or false
	 */

	public boolean delete(User user) {
		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			return true;
		}

		return false;
	}
	
	/***
	 * Deletes this record
	 */

	public void delete() {
		Database db = Database.getInstance();
		db.deleteRecord(id);
		db.updateRecords();
	}
}
