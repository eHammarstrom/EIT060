
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

			Database db = Database.getInstance();

			users = db.getUsers();
			records = db.getRecords();

			User loggedInUser = null;
			boolean isLogin = false;

			User login = null;

			for (User u : users) {
				System.out.println(cert.getSerialNumber().toString());
				login = u.login(cert.getSerialNumber().toString());
				loggedInUser = u;
				if (login != null) {
					isLogin = true;
					break;
				}
			}

			if (isLogin)
				Log.append(cert.getSerialNumber().toString(), Log.LOGIN_SUCCESS);
			else
				Log.append(cert.getSerialNumber().toString(), Log.LOGIN_FAILED);

			oos.writeObject(login);
			oos.flush();

			if (login != null)
				System.out.println("Sent user: \t" + login.toString());
			else
				System.out.println("Sent user: \tNULL");

			if (login != null && !records.isEmpty()) {

				ArrayList<Record> userRecords = getUserRecords(login);

				oos.writeObject(userRecords);
				oos.flush();
			}

			String command = null;

			while ((command = in.readLine()) != null) {
				String[] commandSplit = command.split("\\s+");

				for (String s : commandSplit) {
					System.out.println(s);
				}

				boolean recordAccess = true;
				Record rec = null;

				if (commandSplit[0].equalsIgnoreCase("recordfetch")) {
					System.out.println("RECORD FETCH RECEIVED.");
					Log.append(loggedInUser.toString(), Log.RETR_RECORDS);

					ArrayList<Record> userRecords = getUserRecords(loggedInUser);

					for (Record r : userRecords) {
						System.out.println(r.getId() + " " + r.getMedicalData());
					}

					oos.writeObject(userRecords);
					oos.flush();
				} else if (commandSplit[0].equalsIgnoreCase("create")) {
					Log.append(loggedInUser.toString(), Log.CREATE);

					recordAccess = loggedInUser.createRecord();
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						String[] recordData = in.readLine().split("\\s+");
						String doctor = recordData[0];
						String nurse = recordData[1];
						String patient = recordData[2];
						String division = recordData[3];
						String medicalData = recordData[4];

						Doctor d = (Doctor) db.getUserFromName(doctor);
						Nurse n = (Nurse) db.getUserFromName(nurse);
						Patient p = (Patient) db.getUserFromName(patient);

						if (d != null && n != null && p != null && division != null && medicalData != null) {
							loggedInUser.createRecord(d, n, p, division, medicalData);
						} else {
							System.out.println("CANNOT CREATE RECORD -> NULL EXCEPTION");
						}
					}

					records = db.getRecords();

				} else {
					for (Record r : records) {
						if (Long.parseLong(commandSplit[1]) == r.getId()) {
							System.out.println("Found requested record: " + r.getId());
							rec = r;
							break;
						}
					}

					if (rec == null) {
						System.out.println("RECORD DOES NOT EXIST!");
						break;
					}
				}

				if (commandSplit[0].equalsIgnoreCase("read")) {
					Log.append(loggedInUser.toString(), Log.READ);

					recordAccess = loggedInUser.readRecord(rec);
					returnAccess(recordAccess, printWriter);
				} else if (commandSplit[0].equalsIgnoreCase("edit")) {
					Log.append(loggedInUser.toString(), Log.EDIT);

					recordAccess = loggedInUser.writeRecord(rec);
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						rec.write(in.readLine());
					}

					records = db.getRecords();

				} else if (commandSplit[0].equalsIgnoreCase("delete")) {
					Log.append(loggedInUser.toString(), Log.DELETE);

					recordAccess = loggedInUser.deleteRecord(rec);
					returnAccess(recordAccess, printWriter);

					if (recordAccess) {
						rec.delete();
					}

					records = db.getRecords();
				}
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

	private ArrayList<Record> getUserRecords(User login) {
		if (login == null)
			System.err.println("LOGIN WAS NULL> getUserRecords(login)");

		ArrayList<Record> userRecords = new ArrayList<Record>();

		for (Record r : records) {
			if (login.isAssociated(r)) {
				System.out.println("Found associated record: " + r.getId());
				userRecords.add(r);
			} else if (login.getPermissions().equals(PermissionLevel.Agency)) {
				userRecords.add(r);
			}
		}

		return userRecords;
	}

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

				ks.load(new FileInputStream("serverkeystore"), password); // keystore
																			// password
																			// (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore
																			// password
																			// (storepass)
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
