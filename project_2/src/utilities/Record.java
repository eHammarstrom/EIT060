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
	
	public void read(User user) {
		if (user.getPermissions() == PermissionLevel.Nurse) {
			if (user.getDivision() == division) {
				System.out.println("I can read this.");
			} else {
				System.out.println("I am a nurse, but this is not my division nor my patient.");
			}
		}
	}
	
	public void write(User user) {
		
	}
	
	public void create(User user) {
		
	}
	
	public void delete(User user) {
		
	}

}
