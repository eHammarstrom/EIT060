package tests;

import java.util.ArrayList;

import utilities.Agency;
import utilities.DBFileHandler;
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
		Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen", 11);

		// System.out.println(doc_1.toString());
		// System.out.println(nurse_1.toString());
		// System.out.println(nurse_2.toString());
		// System.out.println(patient_1.toString());

		r.read(nurse_1);
		r.read(doc_1);
		r.read(nurse_2);
		r.read(patient_1);
		r.read(agency_1);
			
		DBFileHandler.appendRecordToDB(r);
		DBFileHandler.appendRecordToDB(r2);
		DBFileHandler.appendUsersToDB(doc_1);
		DBFileHandler.appendUsersToDB(nurse_1);
		DBFileHandler.appendUsersToDB(nurse_2);
		DBFileHandler.appendUsersToDB(patient_1);
		DBFileHandler.appendUsersToDB(agency_1);
		
		ArrayList<User> users = DBFileHandler.loadUsers();
		
		for(User u : users) {
			System.out.println(u.getCertNbr());
		}
		
		ArrayList<Record> records = DBFileHandler.loadRecords();
		
		for(Record rec : records) {
			System.out.println(rec.getMedicalData());
		}


	}

}
