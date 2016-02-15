package tests;

import utilities.Doctor;
import utilities.Nurse;
import utilities.Patient;
import utilities.Record;

public class UserRecordCreation {

	public static void main(String[] args) {
		Doctor doc_1 = new Doctor("doctor", "password", 1);
		Nurse nurse_1 = new Nurse("nurse", "password", 2);
		Patient patient_1 = new Patient("patient", "password", 3);
		Record r = new Record(doc_1, nurse_1, patient_1, "div_1", "Sjuk.", 1);
		
		System.out.println(doc_1.toString());
		System.out.println(nurse_1.toString());
		System.out.println(patient_1.toString());
	}

}
