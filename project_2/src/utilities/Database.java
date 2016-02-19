package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Database {

	private static Connection conn;
	private ArrayList<User> users;
	private ArrayList<Record> records;

	public Database() {
		conn = null;
	}

	public boolean openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://puccini.cs.lth.se/" + "db142", "db142", "classic");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		System.out.println("Connected");

		return true;
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

			while (result.next()) {

				Doctor doctor = (Doctor) getUser(result.getLong("doctor"));
				Nurse nurse = (Nurse) getUser(result.getLong("nurse"));
				Patient patient = (Patient) getUser(result.getLong("patient"));
				String division = result.getString("division");
				String medicalData = result.getString("medicalData");
				long id = result.getLong("id");

				Record r = new Record(doctor, nurse, patient, division, medicalData, id);

				if (r != null) {
					records.add(r);
				}
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

	private User getUser(long certNbr) {
		for (User u : users) {
			if (u.getCertNbr() == certNbr) {
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
				long certNbr = result.getLong("certNbr");

				User u = null;

				if (permLevel.equals("agency")) {
					u = new Agency(username, password, division, certNbr, true);
				}

				if (permLevel.equals("doctor")) {
					u = new Doctor(username, password, division, certNbr, true);
				}

				if (permLevel.equals("nurse")) {
					u = new Nurse(username, password, division, certNbr, true);
				}

				if (permLevel.equals("patient")) {
					u = new Patient(username, password, division, certNbr, true);
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
			statement.setLong(5, u.getCertNbr());
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
			statement.setLong(1, r.getDoctorCertNbr());
			statement.setLong(2, r.getNurseCertNbr());
			statement.setLong(3, r.getPatientCertNbr());
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
	
	public void loadTestData() {
		Doctor doc_1 = new Doctor("doctor", "password", User.DIV_EMERGENCY, 1, false);
		Nurse nurse_1 = new Nurse("nurse", "password", User.DIV_REHAB, 2, false);
		Nurse nurse_2 = new Nurse("nurse2", "password", User.DIV_EMERGENCY, 3, false);
		Patient patient_1 = new Patient("patient", "password", User.DIV_REHAB, 4, false);
		Agency agency_1 = new Agency("Agency", "password", User.DIV_REHAB, 5, false);

		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i benet", 10);
		Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen", 11);
		
		this.insertUser(doc_1);
		this.insertUser(nurse_1);
		this.insertUser(nurse_2);
		this.insertUser(patient_1);
		this.insertUser(agency_1);
		
		this.insertRecord(r);
		this.insertRecord(r2);
	}

}
