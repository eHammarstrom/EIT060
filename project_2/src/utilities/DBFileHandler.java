package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DBFileHandler {
	private static File file;
	private static FileWriter fileWriter;
	private static ArrayList<User> users;
	private static ArrayList<Record> records;

	public static ArrayList<User> loadUsers() {

		users = new ArrayList<User>();

		try {

			Scanner scan = new Scanner(new File("users.json"), "UTF-8");

			while (scan.hasNext()) {

				JSONObject obj = (JSONObject) new JSONParser().parse(scan.nextLine());

				String username = (String) obj.get("Username");
				String password = (String) obj.get("Password");
				String division = (String) obj.get("Division");
				long id = (long) obj.get("Id");
				String permLevel = (String) obj.get("Permission level");

				User u = null;

				if (permLevel.equals("Doctor")) {
					u = new Doctor(username, password, division, id);
				}

				if (permLevel.equals("Nurse")) {
					u = new Nurse(username, password, division, id);
				}

				if (permLevel.equals("Patient")) {
					u = new Patient(username, password, division, id);
				}

				if (permLevel.equals("Agency")) {
					u = new Agency(username, password, division, id);
				}

				users.add(u);
			}
			
			scan.close();
			return users;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void appendUsersToDB(User u) {

		try {
			file = new File("users.json");
			file.createNewFile();
			fileWriter = new FileWriter(file, true);
			System.out.println("Created!");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JSONObject obj = new JSONObject();

		obj.put("Username", u.getUsername());
		obj.put("Password", u.getPassword());
		obj.put("Division", u.getDivision());
		obj.put("Id", u.getCertNbr());
		obj.put("Permission level", u.getPermissions().toString());

		try {

			System.out.println("Added user");
			fileWriter.write(obj.toJSONString() + "\n");
			fileWriter.flush();
			// fileWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block

			System.out.println("Write to JSON file failure!");

			e.printStackTrace();
		}

	}

	private static User getUserWithCertNbr(long id) {
		for (User u : users) {
			if (u.getCertNbr() == id) {
				return u;
			}
		}
		return null;
	}

	public static ArrayList<Record> loadRecords() {

		records = new ArrayList<Record>();

		try {

			Scanner scan = new Scanner(new File("records.json"), "UTF-8");

			while (scan.hasNext()) {

				JSONObject obj = (JSONObject) new JSONParser().parse(scan.nextLine());

				Long doctor = (Long) obj.get("Doctor");
				Long nurse = (Long) obj.get("Nurse");
				Long patient = (Long) obj.get("Patient");
				String division = (String) obj.get("Division");
				String medicalData = (String) obj.get("Medical data");
				long id = (long) obj.get("Id");

				Doctor d = (Doctor) getUserWithCertNbr(doctor);
				Nurse n = (Nurse) getUserWithCertNbr(nurse);
				Patient p = (Patient) getUserWithCertNbr(patient);

				Record r = new Record(d, n, p, division, medicalData, id);

				records.add(r);
			}
			
			scan.close();
			return records;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static void appendRecordToDB(Record r) {

		try {
			file = new File("records.json");
			file.createNewFile();
			fileWriter = new FileWriter(file, true);
			System.out.println("Created!");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JSONObject obj = new JSONObject();
		obj.put("Doctor", r.getDoctorCertNbr());
		obj.put("Nurse", r.getNurseCertNbr());
		obj.put("Patient", r.getPatientCertNbr());
		obj.put("Division", r.getDivision());
		obj.put("Medical data", r.getMedicalData());
		obj.put("Id", r.getId());

		try {

			System.out.println("Added record");
			fileWriter.write(obj.toJSONString() + "\n");
			fileWriter.flush();
			// fileWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block

			System.out.println("Write to JSON file failure!");

			e.printStackTrace();
		}

	}

}
