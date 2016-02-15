package tests;

import utilities.Doctor;
import utilities.Nurse;
import utilities.Patient;
import utilities.Record;
import utilities.User;

public class UserRecordCreation {

	public static void main(String[] args) {

		Doctor doc_1 = new Doctor("doctor", "password", User.DIV_EMERGENCY, 1);
		Nurse nurse_1 = new Nurse("nurse", "password", User.DIV_REHAB, 2);
		Nurse nurse_2 = new Nurse("nurse 2", "password", User.DIV_EMERGENCY, 4);
		Patient patient_1 = new Patient("patient", "password", User.DIV_REHAB, 3);
		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Sjuk.", 1);
		
		System.out.println(doc_1.toString());
		System.out.println(nurse_1.toString());
		System.out.println(nurse_2.toString());
		System.out.println(patient_1.toString());
		
		r.read(nurse_2);
	}

}
