package client;

import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import utilities.PermissionLevel;
import utilities.Record;
import utilities.User;

import java.security.KeyStore;
import java.util.ArrayList;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class client {
	private static User user;
	private static ArrayList<Record> records;

	private static BufferedReader read;
	private static BufferedReader serverMsg;
	private static PrintWriter out;
	private static ObjectInputStream ois;

	private static String msg;
	private static String[] splitMsg;

	public static void main(String[] args) throws Exception {
		String host = null;
		int port = -1;
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		if (args.length < 2) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}
		try { /* get input parameters */
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}

		try { /* set up a key manager for client authentication */
			SSLSocketFactory factory = null;
			try {
				char[] password = "password".toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				ks.load(new FileInputStream("clientkeystore"), password); // keystore
																			// password
																			// (storepass)
				ts.load(new FileInputStream("clienttruststore"), password); // truststore
																			// password
																			// (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			System.out.println("Handshake socket: " + socket + "\n");

			/*
			 * send http request
			 *
			 * See SSLSocketClient.java for more information about why there is
			 * a forced handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			System.out.println("Server DN: " + cert.getSubjectDN().getName());
			System.out.println("Handshake socket: " + socket);
			System.out.println("Secure connection.");
			System.out.println("Issuer DN: " + cert.getIssuerDN().getName());
			System.out.println("Serial N: " + cert.getSerialNumber().toString());

			read = new BufferedReader(new InputStreamReader(System.in));
			serverMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			ois = new ObjectInputStream(socket.getInputStream());
			records = new ArrayList<Record>();

			boolean isShutdown = false;
			boolean isLoggedIn = false;
			boolean isDone = false;

			while (!isLoggedIn) {
				System.out.println("Provide login command: login <username> <password> \n");
				System.out.print("Login >");

				msg = read.readLine();

				if (msg.equalsIgnoreCase("quit")) {
					isShutdown = true;
					break;
				}

				out.println(msg);
				out.flush();

				isLoggedIn = waitForLoginData();
			}

			if (isShutdown) {
				out.close();
				read.close();
				socket.close();

				return;
			}

			while (!isDone) {
				System.out.println("Type help for commands.\n");
				System.out.print(user.getUsername() + " commands>");
				msg = read.readLine();
				splitMsg = msg.split("\\s+");
				
				for(String s : splitMsg) {
					System.out.println(s);
				}
				
				if (msg.equalsIgnoreCase("quit")) {
					break;
				} else if (splitMsg[0].equalsIgnoreCase("help")) {
					printHelp();
				} else if (splitMsg[0].equalsIgnoreCase("records")) {
					printRecords();
				} else if (splitMsg[0].equalsIgnoreCase("edit")) {
					out.println(msg);
					out.flush();

					if (serverMsg.readLine().equalsIgnoreCase("yes")) {
						editRecord(splitMsg[1]);
					}
				} else if (splitMsg[0].equalsIgnoreCase("read")) {
					System.out.println("Inside read");
					if (recordExists(splitMsg[1])) {
						System.out.println("Inside read if-state");
						out.println(msg);
						out.flush();
					} else {
						System.out.println("Record does not exists.");
					}

					if (serverMsg.readLine().equalsIgnoreCase("yes")) {
						printRecord(splitMsg[1]);
					} else {
						System.out.println("Access denied.");
					}
				} else if (splitMsg[0].equalsIgnoreCase("delete") && user.getPermissions() == PermissionLevel.Agency) {
					if (recordExists(splitMsg[1])) {
						out.println(msg);
						out.flush();
					} else {
						System.out.println("Record does not exists.");
					}

					if (serverMsg.readLine().equalsIgnoreCase("yes")) {
						System.out.println("Record deleted.");
						
						for (Record r : records) {
							if (r.getId() == Long.parseLong(splitMsg[1])) {
								r.delete(user);
							}
						}
					} else {
						System.out.println("Access denied.");
					}
				}
			}

			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean recordExists(String rNbr) {
		boolean exists = false;

		for (Record r : records) {
			if (r.getId() == Long.parseLong(splitMsg[1])) {
				exists = true;
			}
		}

		return exists;
	}

	private static void editRecord(String rNbr) throws IOException {
		Record record = null;
		boolean isDone = false;

		for (Record r : records) {
			if (r.getId() == Long.parseLong(rNbr)) {
				record = r;
			}
		}

		if (record == null) {
			System.out.println("Given record does not exist.");
		} else if (serverMsg.readLine().equalsIgnoreCase("no")) {
			System.out.println("You do not have the permissions to edit this record.");
		} else {
			while (!isDone) {
				System.out.print(record.getMedicalData());

				msg = read.readLine();

				System.out.println("Done editing or start over? <yes>/<no>/<cancel>");
				String ans = read.readLine();

				if (ans.equalsIgnoreCase("yes")) {
					out.println(msg);
					out.flush();
					isDone = true;
					System.out.println("Successfully edited record.");
				}
			}
		}
	}

	private static void printHelp() {
		System.out.println("records - This retrieves a list of available records.");
		System.out.println("read <record nbr>");

		if (user.getPermissions() != PermissionLevel.Patient)
			System.out.println("edit <record nbr>");

		if (user.getPermissions() == PermissionLevel.Agency)
			System.out.println("delete <record nbr>");
	}

	private static void printRecords() {
		for (Record r : records) {
			System.out.println(r.toString());
		}
	}

	private static void printRecord(String rNbr) {
		for (Record r : records) {
			if (r.getId() == Long.parseLong(rNbr)) {
				System.out.println(r.getMedicalData());
			}
		}
	}

	private static boolean waitForLoginData() throws IOException, ClassNotFoundException {
		user = (User) ois.readObject();

		if (user != null) {
			System.out.println(user.toString());
			
			records = (ArrayList<Record>) ois.readObject();

//			for (;;) {
//				try {
//					records.add((Record) ois.readObject());
//				} catch (Exception e) {
//					ois.close();
//					break;
//				}
//			}
			
		} else {
			return false;
		}

		if (!records.isEmpty()) {
			for (Record r : records) {
				System.out.println(r.toString());
			}
		}

		return true;
	}

}
