package utilities;

import java.io.Serializable;

public class Record implements Serializable {

	private Doctor doctor;
	private Nurse nurse;
	private Patient patient;
	private String division;
	private String medicalData;
	private long id;

	public Record(Doctor doctor, Nurse nurse, Patient patient, String division, String medicalData, long id) {
		this.doctor = doctor;
		this.nurse = nurse;
		this.patient = patient;
		this.division = division;
		this.medicalData = medicalData;
		this.id = id; // This should be setup to be auto incremented when added
						// to the DB.

		doctor.addRecord(this);
		nurse.addRecord(this);
		patient.addRecord(this);
	}

	public long getDoctorCertNbr() {
		return doctor.getCertNbr();
	}

	public long getNurseCertNbr() {
		return nurse.getCertNbr();
	}

	public long getPatientCertNbr() {
		return patient.getCertNbr();
	}

	public String getDivision() {
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

	public boolean read(User user) {
		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Doctor)
				&& (user.getDivision().equals(division) || user.getCertNbr() == doctor.getCertNbr())) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Nurse)
				&& (user.getDivision().equals(division) || user.getCertNbr() == nurse.getCertNbr())) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Patient)
				&& user.getCertNbr() == patient.getCertNbr()) {
			return true;
		}
			
		return false;
	}

	public boolean write(User user) {
		if (user.getPermissions().equals(PermissionLevel.Doctor) && user.getDivision().equals(doctor.getDivision())) {
			return true;
		} else if (user.getPermissions().equals(PermissionLevel.Nurse) && user.getCertNbr() == nurse.getCertNbr()) {
			return true;
		}

		return false;
	}

	public void write(String data) {
		medicalData = data;
		// here we need to write to the DB file, cause change.
	}

	public boolean delete(User user) {
		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			return true;
		}

		return false;
	}

	public void delete() {
		// Here we must update doctor, nurse, and patient of their loss of
		// record and then write this to the DB
	}

}
