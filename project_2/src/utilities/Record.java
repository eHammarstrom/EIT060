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
		this.id = id;

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
		String operation = "DENIED READ ACCESS";

		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			System.out.println("I can read this. /Agency");
			operation = "READ";
		}

		if (user.getPermissions().equals(PermissionLevel.Doctor)) {
			if (user.getDivision().equals(division) || user.getCertNbr() == doctor.getCertNbr()) {
				System.out.println("I can read this. /Doctor");
				operation = "READ";
			} else {
				System.out.println("I am a doctor, but this is not my division nor my patient!");
				operation = "DENIED READ ACCESS";
			}
		}

		if (user.getPermissions().equals(PermissionLevel.Nurse)) {
			if (user.getDivision().equals(division)) {
				System.out.println("I can read this. /Nurse");
				operation = "READ";
			} else {
				System.out.println("I am a nurse, but this is not my division nor my patient.");
				operation = "DENIED READ ACCESS";
			}
		}

		if (user.getPermissions().equals(PermissionLevel.Patient)) {
			if (user.getCertNbr() == patient.getCertNbr()) {
				System.out.println("I can read this. /Patient");
				operation = "READ";
			} else {
				System.out.println("DENIED READ ACCESS!");
			}
		}
		
		Log.append(user.toString(), operation);
		
		if(operation.equals("READ")) {
			return true;
		} else {
			return false;
		}

	}

	public boolean write(User user) {
		String operation = "DENIED WRITE ACCESS";

		if (user.getPermissions().equals(PermissionLevel.Doctor)) {
			if (user.getCertNbr() == doctor.getCertNbr()) {
				System.out.println("I can write to this. /Doctor");
				operation = "WRITE";
			} else {
				System.out.println("I am a doctor, but this is not my division nor my patient!");
				operation = "DENIED WRITE ACCESS";
			}
		}

		if (user.getPermissions().equals(PermissionLevel.Nurse)) {
			if (user.getDivision().equals(division) && user.getCertNbr() == nurse.getCertNbr()) {
				System.out.println("I can write to this. /Nurse");
				operation = "WRITE";
			} else {
				System.out.println("I am a nurse, but this is not my division nor my patient.");
				operation = "DENIED WRITE ACCESS";
			}
		}

		Log.append(user.toString(), operation);
		
		if(operation.equals("WRITE")) {
			return true;
		} else {
			return false;
		}

	}

	public boolean create(User user) {
		String operation = "DENIED CREATE ACCESS";

		if (user.getPermissions().equals(PermissionLevel.Doctor) && user.getCertNbr() == doctor.getCertNbr()) {
			System.out.println("I can creat new record. /Doctor");
			operation = "CREATE";
		} else {
			operation = "DENIED CREATE ACCESS";
		}

		Log.append(user.toString(), operation);
		
		if(operation.equals("WRITE")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean delete(User user) {
		String operation = "DENIED DELETE ACCESS";

		if (user.getPermissions().equals(PermissionLevel.Agency)) {
			operation = "DELETE";
		} else {
			operation = "DENIED DELETE ACCESS";
		}

		Log.append(user.toString(), operation);
		
		if(operation.equals("WRITE")) {
			return true;
		} else {
			return false;
		}

	}

}
