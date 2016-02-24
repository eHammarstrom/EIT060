package client;

import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

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
	private static char[] password;

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
				// char[] password = "password".toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				System.out.print("Enter keystore: ");

				String keystoreName = br.readLine();

				// System.out.print("\nEnter password: ");

				// String pwRead = br.readLine();
				// char[] password = pwRead.toCharArray();

				Console cons = System.console();

				if (cons != null) {
					password = cons.readPassword("%s", "Password: ");
				} else {
					throw new IOException(
							"Cannot find a console to read password from. Eclipse CANNOT fork a terminal child process.");
				}

				/**
				 * JAVA INVISIBLE READ EXAMPLE
				 * 
				 * Console cons; char[] passwd; if ((cons = System.console()) !=
				 * null && (passwd = cons.readPassword("[%s]", "Password:")) !=
				 * null) { ... java.util.Arrays.fill(passwd, ' '); }
				 * 
				 */

				ks.load(new FileInputStream("keystores/" + keystoreName), password); // keystore
				// password
				// (storepass)
				char[] cliTrustPW = "password".toCharArray();
				ts.load(new FileInputStream("clienttruststore"), cliTrustPW); // truststore
																				// password
																				// (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				e.printStackTrace();
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

			boolean isLoggedIn = false;
			boolean isDone = false;

			isLoggedIn = waitForLoginData();

			if (!isLoggedIn) {
				System.out.println("This certificate does not have a user. \t Press the RETURN key to exit.");
				System.console().readLine();

				out.close();
				read.close();
				socket.close();
				return;
			}

			boolean accessDenied = false;

			while (!isDone) {
				if (accessDenied) {
					System.out.println("Access denied, or no such record exists! \t Type 'help' for commands.");
				}

				System.out.print(user.getUsername() + " commands>");
				msg = read.readLine();
				splitMsg = msg.split("\\s+");

				try {
					if (msg.equalsIgnoreCase("quit")) {
						break;
					} else if (msg.equalsIgnoreCase("help")) {
						printHelp();
					} else if (splitMsg[0].equalsIgnoreCase("records")) {
						printRecords();
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("edit") && recordExists(splitMsg[1])
							&& (accessDenied = hasPermissions(msg))) {
						editRecord(splitMsg[1]);
						fetchRecords();
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("read") && recordExists(splitMsg[1])
							&& (accessDenied = hasPermissions(msg))) {
						printRecord(splitMsg[1]);
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("delete") && recordExists(splitMsg[1])
							&& (accessDenied = hasPermissions(msg))) {
						for (Record r : records) {
							if (r.getId() == Long.parseLong(splitMsg[1])) {
								r.delete(user);
								accessDenied = false;
							}
						}

						fetchRecords();
					} else if (splitMsg[0].equalsIgnoreCase("create") && (accessDenied = hasPermissions(msg))) {
						createRecord();
						fetchRecords();
						accessDenied = false;
					} else {
						accessDenied = true;
					}
				} catch (Exception e) {
					accessDenied = true;
				}
			}

			ois.close();
			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fetchRecords() throws ClassNotFoundException, IOException {
		out.println("recordfetch");
		out.flush();

		records = (ArrayList<Record>) ois.readObject();

		System.out.println("Printing fetched records: ");
		for (Record r : records)
			System.out.println(r.getId() + " " + r.getMedicalData());
	}

	private static boolean hasPermissions(String msg) throws IOException {
		out.println(msg);
		out.flush();

		if (serverMsg.readLine().equalsIgnoreCase("granted")) {
			return true;
		}

		return false;
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

		while (!isDone) {
			System.out.println("OLD: " + record.getMedicalData());
			System.out.print("NEW: ");
			msg = read.readLine();
			System.out.println("Save change? <yes>/<no>");
			String ans = read.readLine();

			if (ans.equalsIgnoreCase("yes")) {
				out.println(msg);
				out.flush();
				isDone = true;
				System.out.println("Successfully edited record.");
			}
		}
	}

	private static void createRecord() throws IOException {
		boolean isDone = false;

		while (!isDone) {
			System.out.println("CREATE as <doctorName> <nurseName> <patientName> <division> <medicalData>: ");
			msg = read.readLine();
			System.out.println("Save created record? <yes>/<no>");
			String ans = read.readLine();

			if (ans.equalsIgnoreCase("yes")) {
				out.println(msg);
				out.flush();
				isDone = true;
				System.out.println("Successfully created record.");
			}
		}

		if (!serverMsg.readLine().equals("created"))
			System.out.println("Input error.");
	}

	private static void printHelp() {
		System.out.println("quit - Exits program.");
		System.out.println("records - This retrieves a list of available records.");
		System.out.println("read <record nbr>");
		System.out.println("edit <record nbr>");
		System.out.println("delete <record nbr>");
		System.out.println("create");
	}

	private static void printRecords() {
		for (Record r : records) {
			System.out.println(r.toString());
		}
	}

	private static void printRecord(String rNbr) {
		for (Record r : records) {
			if (r.getId() == Long.parseLong(rNbr) && r != null) {
				System.out.println(r.getMedicalData());
			}
		}
	}

	private static boolean waitForLoginData() throws IOException, ClassNotFoundException {
		user = (User) ois.readObject();

		if (user != null) {
			System.out.println(user.toString());

			records = (ArrayList<Record>) ois.readObject();

		} else {
			return false;
		}


		if (!records.isEmpty()) {
			System.out.println("Associated records: ");

			for (Record r : records) {
				System.out.println(r.toString());
			}
		} else {
			System.out.println("No associated records.");
		}

		return true;
	}

}
