package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mysql.jdbc.Statement;

public class Database {

	private static Connection conn;
	private ArrayList<User> users;
	private ArrayList<Record> records;
	private static String db_url;
	private static String db_class;
	private static String db_name;
	private static String db_password;

	private Database() {
		db_class = "com.mysql.jdbc.Driver";
		db_url = "jdbc:mysql://puccini.cs.lth.se/";
		db_name = "db142";
		db_password = "classic";

		/* Creation of an instance of the connection statement */

		conn = setConnection();
	}

	/* Private method charge to set the connection statement */

	private static Connection setConnection() {
		try {
			try {
				Class.forName(db_class);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			java.sql.Connection conn = DriverManager.getConnection(db_url + db_name, db_name, db_password);
			return conn;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/*
	 * Private inner class responsible for instantiating the single instance of
	 * the singleton
	 */

	private static class DatabaseHolder {
		private final static Database instance = new Database();
	}

	/**
	 * Public method, which is the only method allowed to return an instance of
	 * the singleton (the instance here is the database connection statement)
	 */

	public static Database getInstance() {
		try {
			return DatabaseHolder.instance;
		} catch (ExceptionInInitializerError ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
		}
		conn = null;
	}

	public boolean isConnected() {
		return conn != null;
	}

	public ArrayList<Record> getRecords() {

		PreparedStatement statement = null;
		try {
			String sql = "SELECT doctor, nurse, patient, division, medicalData, id FROM records";
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			records = new ArrayList<Record>();

			while (result.next()) {

				String doctorCertNbr = result.getString("doctor");
				String nurseCertNbr = result.getString("nurse");
				String patientCertNbr = result.getString("patient");
				String division = result.getString("division");
				String medicalData = result.getString("medicalData");
				long id = result.getLong("id");

				Doctor doctor = null;
				Nurse nurse = null;
				Patient patient = null;

				for (User u : users) {
					if (u.getCertNbr().equals(doctorCertNbr)) {
						doctor = (Doctor) u;
					} else if (u.getCertNbr().equals(nurseCertNbr)) {
						nurse = (Nurse) u;
					} else if (u.getCertNbr().equals(patientCertNbr)) {
						patient = (Patient) u;
					}
				}

				Record r = null;

				if (doctor != null && nurse != null && patient != null) {
					r = new Record(doctor, nurse, patient, division, medicalData, id);
				} else {
					System.out.println("NULL FAILURE!");
				}

				records.add(r);
			}

			return records;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	private Record getRecord(long id) {
		for (Record r : records) {
			if (r.getId() == id) {
				return r;
			}
		}
		return null;
	}

	private User getUser(String certNbr) {
		for (User u : users) {
			if (u.getCertNbr().equals(certNbr)) {
				return u;
			}
		}
		return null;
	}

	public ArrayList<User> getUsers() {

		PreparedStatement statement = null;
		try {
			String sql = "SELECT username, password, division, certNbr, permissionLevel, certNbr FROM users2";
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			users = new ArrayList<User>();

			while (result.next()) {

				String permLevel = result.getString("permissionLevel");
				String username = result.getString("username");
				String password = result.getString("password");
				String division = result.getString("division");
				String certNbr = result.getString("certNbr");

				User u = null;

				if (permLevel.equalsIgnoreCase("agency")) {
					u = new Agency(username, password, division, certNbr, true);
					System.out.println("CREATED AGENCY: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("doctor")) {
					u = new Doctor(username, password, division, certNbr, true);
					System.out.println("CREATED DOCTOR: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("nurse")) {
					u = new Nurse(username, password, division, certNbr, true);
					System.out.println("CREATED NURSE: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("patient")) {
					u = new Patient(username, password, division, certNbr, true);
					System.out.println("CREATED PATIENT: " + u.toString());
				}

				if (u != null) {
					users.add(u);
				}
			}

			return users;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void insertUser(User u) {

		PreparedStatement statement = null;
		try {
			String sql = "INSERT INTO users2(username, password, division, permissionLevel, certNbr) VALUES(?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, u.getUsername());
			statement.setString(2, u.getPassword());
			statement.setString(3, u.getDivision());
			statement.setString(4, u.getPermissions().toString());
			statement.setString(5, u.getCertNbr());
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void insertRecord(Record r) {

		PreparedStatement statement = null;
		try {
			String sql = "INSERT INTO records(doctor, nurse, patient, division, medicalData, id) VALUES(?,?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, r.getDoctorCertNbr());
			statement.setString(2, r.getNurseCertNbr());
			statement.setString(3, r.getPatientCertNbr());
			statement.setString(4, r.getDivision());
			statement.setString(5, r.getMedicalData());
			statement.setLong(6, r.getId());
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeRecord(String medicalData, long id) {
		PreparedStatement statement = null;
		try {
			String sql = "UPDATE records SET medicalData = ? WHERE id = ?";
			statement = conn.prepareStatement(sql);
			statement.setString(1, medicalData);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteRecord(long id) {
		PreparedStatement statement = null;
		try {
			String sql = "DELETE FROM records WHERE id = ?";
			statement = conn.prepareStatement(sql);
			statement.setLong(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateRecords() {
		getRecords();
	}

	public void updateUsers() {
		getUsers();
	}

	public void loadTestData() {
		Doctor doc_1 = new Doctor("doctor", "password", User.DIV_EMERGENCY, "13334610649522941717", false);
		Nurse nurse_1 = new Nurse("nurse", "password", User.DIV_REHAB, "2", false);
		Nurse nurse_2 = new Nurse("nurse2", "password", User.DIV_EMERGENCY, "3", false);
		Patient patient_1 = new Patient("patient", "password", User.DIV_REHAB, "4", false);
		Patient patient_2 = new Patient("patient2", "password", User.DIV_REHAB, "5", false);
		Agency agency_1 = new Agency("agency", "password", User.DIV_REHAB, "6", false);

		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i benet", 10);
		Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen", 11);
		Record r3 = new Record(doc_1, nurse_2, patient_2, User.DIV_EMERGENCY, "Ont i huvudet", 12);
		Record r4 = new Record(doc_1, nurse_2, patient_2, User.DIV_EMERGENCY, "Ont i örat", 13);

		this.insertUser(doc_1);
		this.insertUser(nurse_1);
		this.insertUser(nurse_2);
		this.insertUser(patient_1);
		this.insertUser(patient_2);
		this.insertUser(agency_1);

		this.insertRecord(r);
		this.insertRecord(r2);
		this.insertRecord(r3);
		this.insertRecord(r4);
	}

}
