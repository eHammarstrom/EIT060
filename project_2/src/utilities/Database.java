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
	private static ArrayList<User> users;
	private static ArrayList<Record> records;

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
			records = new ArrayList<Record>();

			while (result.next()) {

				Doctor doctor = (Doctor) getUser(result.getLong("doctor"));
				Nurse nurse = (Nurse) getUser(result.getLong("nurse"));
				Patient patient = (Patient) getUser(result.getLong("patient"));
				String division = result.getString("division");
				String medicalData = result.getString("medicalData");
				long id = result.getLong("certNbr");

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

	private User getUser(long certNbr) {
		for (User u : users) {
			if (u.getCertNbr() == certNbr) {
				return u;
			}
		}
		return null;
	}

	public static ArrayList<User> getUsers() {

		PreparedStatement statement = null;
		try {
			String sql = "SELECT username, password, division, certNbr, permissionLevel, certNbr FROM users2";
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			ArrayList<User> users = new ArrayList<User>();

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

	public static void main(String[] args) {
		Database db = new Database();
		db.openConnection();
		ArrayList<User> list = getUsers();
		for (User u : list) {
			System.out.println(u.getUsername() + " " + u.getCertNbr());
		}
	}
}
