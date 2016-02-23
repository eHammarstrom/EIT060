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

		Doctor doc_1 = new Doctor("doctor", "password", User.DIV_EMERGENCY, "1", false);
		Nurse nurse_1 = new Nurse("nurse", "password", User.DIV_REHAB, "2", false);
		Nurse nurse_2 = new Nurse("nurse2", "password", User.DIV_EMERGENCY, "3", false);
		Patient patient_1 = new Patient("patient", "password", User.DIV_REHAB, "4", false);
		Agency agency_1 = new Agency("Agency", "password", User.DIV_REHAB, "5", false);

		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i benet");
		Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen");

		// System.out.println(doc_1.toString());
		// System.out.println(nurse_1.toString());
		// System.out.println(nurse_2.toString());
		// System.out.println(patient_1.toString());
			
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
			User login = u.login("1");

			if (u.getUsername().equals("doctor"))
				System.out.println(u.getPassword());
				
			if (login != null && login.getUsername().equals(u.getUsername()))
				System.out.println("Logged in to doctor.");
		}
		
		ArrayList<Record> records = DBFileHandler.loadRecords();
		
		for(Record rec : records) {
			System.out.println(rec.getMedicalData());
		}

	}

}
