
package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import utilities.Database;
import utilities.Division;
import utilities.Doctor;
import utilities.Log;
import utilities.Nurse;
import utilities.Patient;
import utilities.PermissionLevel;
import utilities.User;
import utilities.Record;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private ArrayList<User> users;
	private ArrayList<Record> records;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();

	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			numConnectedClients++;
			System.out.println(numConnectedClients + " active connections.");
			System.out.println("Client connection received: ");
			System.out.println("User DN: " + cert.getSubjectDN().getName());
			System.out.println("Issuer DN: " + cert.getIssuerDN().getName());
			System.out.println("Serial N: " + cert.getSerialNumber().toString());

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
			
			/**
			 *  Get the data from the database instance
			 */

			Database db = Database.getInstance();
			users = db.getUsers();
			records = db.getRecords();

			boolean isLogin = false;
			User login = null;

			/**
			 * Check if connected client is a user according to their certificate.
			 */
			for (User u : users) {
				login = u.login(cert.getSerialNumber().toString());
				if (login != null) {
					isLogin = true;
					break;
				}
			}

			// Log login status, access/deny.
			Log.append(cert.getSerialNumber().toString(), Log.LOGIN, isLogin);

			// First post user to client.
			oos.writeObject(login);
			oos.flush();

			// If user is associated to records post them to client.
			if (login != null && !records.isEmpty()) {
				ArrayList<Record> userRecords = getUserRecords(login);

				oos.writeObject(userRecords);
				oos.flush();
			}

			String command = null;

			/***
			 * Await String command input from this client connection.
			 */
			while ((command = in.readLine()) != null) {
				String[] commandSplit = command.split("\\s+");

				boolean recordAccess = true;
				Record rec = null;

				if (commandSplit[0].equalsIgnoreCase("recordfetch")) {
					ArrayList<Record> userRecords = getUserRecords(login);

					oos.writeObject(userRecords);
					oos.flush();
				} else if (commandSplit[0].equalsIgnoreCase("create")) {
					recordAccess = login.createRecord();
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						String[] recordData = in.readLine().split("\\s+");
						String nurse = recordData[1];
						String patient = recordData[2];
						String division = recordData[3];
						String medicalData = "";

						for (int i = 4; i < recordData.length; i++)
							medicalData += recordData[i] + " ";

						User d = login;
						User n = db.getUserFromName(nurse);
						User p = db.getUserFromName(patient);
						Division div = db.getDivision(division);
						

						if (d != null && n != null && p != null && division != null && medicalData != null) {
							login.createRecord((Doctor) d, (Nurse) n, (Patient) p, div, medicalData);
							printWriter.println("created");
							printWriter.flush();
						} else {
							printWriter.println("error");
							printWriter.flush();
						}
					}

					records = db.updateRecords();
				} else {
					for (Record r : records) {
						if (Long.parseLong(commandSplit[1]) == r.getId()) {
							rec = r;
							break;
						}
					}
				}

				if (commandSplit[0].equalsIgnoreCase("read")) {
					recordAccess = login.readRecord(rec);
					returnAccess(recordAccess, printWriter);
				} else if (commandSplit[0].equalsIgnoreCase("edit")) {
					recordAccess = login.writeRecord(rec);
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						rec.write(in.readLine());
						records = db.updateRecords();
					}
				} else if (commandSplit[0].equalsIgnoreCase("delete")) {
					recordAccess = login.deleteRecord(rec);
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						rec.delete();
						records = db.updateRecords();
					}
				}

				Log.append(login.toString(), command, recordAccess);
			}

			printWriter.close();
			oos.close();
			in.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (

		IOException e)

		{
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}

	}

	/***
	 * Retrieves all records associated to a user according to the ACL.
	 * @param login to associate records.
	 * @return array of associated records.
	 */
	private ArrayList<Record> getUserRecords(User login) {
		if (login == null)
			System.err.println("LOGIN WAS NULL> getUserRecords(login)");

		ArrayList<Record> userRecords = new ArrayList<Record>();

		for (Record r : records) {
			if (login.isAssociated(r)) {
				userRecords.add(r);
			} else if (login.getPermissions().equals(PermissionLevel.Agency)) {
				userRecords.add(r);
			}
		}

		return userRecords;
	}

	/***
	 * Returns a String answer over the TLS connection to connected client.
	 * @param ans
	 * @param printWriter
	 */
	private void returnAccess(boolean ans, PrintWriter printWriter) {
		if (ans == true) {
			printWriter.println("granted");
			printWriter.flush();
		} else {
			printWriter.println("denied");
			printWriter.flush();
		}
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client
															// authentication
			new server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				ks.load(new FileInputStream("serverkeystore"), password); // keystore password (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore password (storepass)
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
}
