package utilities;

public class Record {

	private Doctor doctor;
	private Nurse nurse;
	private Patient patient;
	private String division;
	private String medicalData;
	private int id;

	public Record(Doctor doctor, Nurse nurse, Patient patient, String division, String medicalData, int id) {
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
	
	public int getDoctorCertNbr() {
		return doctor.getCertNbr();
	}
	
	public int getNurseCertNbr() {
		return nurse.getCertNbr();
	}
	
	public int getPatientCertNbr() {
		return patient.getCertNbr();
	}
	
	public String getDivision() {
		return division;
	}
	
	public String getMedicalData() {
		return medicalData;
	}
	
	public int getId() {
		return id;
	}

	public String toString() {
		return Integer.toString(id);
	}

	public void read(User user) {
		String operation = "OPERATION";

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

	}

	public void write(User user) {

	}

	public void create(User user) {

	}

	public void delete(User user) {

	}

}
