package tests;

import utilities.Agency;
import utilities.Db;
import utilities.Doctor;
import utilities.Nurse;
import utilities.Patient;
import utilities.Record;
import utilities.User;

public class UserRecordCreation {

	public static void main(String[] args) {

		Doctor doc_1 = new Doctor("doctor", "password", User.DIV_EMERGENCY, 1);
		Nurse nurse_1 = new Nurse("nurse", "password", User.DIV_REHAB, 2);
		Nurse nurse_2 = new Nurse("nurse2", "password", User.DIV_EMERGENCY, 3);
		Patient patient_1 = new Patient("patient", "password", User.DIV_REHAB, 4);
		Agency agency_1 = new Agency("Agency", "password", User.DIV_REHAB, 5);

		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i benet", 10);
	//	Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen", 11);

		// System.out.println(doc_1.toString());
		// System.out.println(nurse_1.toString());
		// System.out.println(nurse_2.toString());
		// System.out.println(patient_1.toString());

		r.read(nurse_1);
		r.read(doc_1);
		r.read(nurse_2);
		r.read(patient_1);
		r.read(agency_1);

		
	//	Db.appendToDb(r);





	}

}
