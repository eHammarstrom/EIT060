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
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				System.out.print("Enter keystore: ");
				String keystoreName = br.readLine();
				Console cons = System.console();

				if (cons != null) {
					password = cons.readPassword("%s", "Password: ");
				} else {
					throw new IOException(
							"Cannot find a console to read password from. Eclipse CANNOT fork a terminal child process.");
				}

				ks.load(new FileInputStream("keystores/" + keystoreName), password); // keystore password (storepass)
				char[] cliTrustPW = "password".toCharArray();
				ts.load(new FileInputStream("clienttruststore"), cliTrustPW); // truststore password (storepass);
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
				System.out.println("This certificate does not have a user. \n Press the RETURN key to exit.");
				System.console().readLine();

				out.close();
				read.close();
				socket.close();
				return;
			}

			boolean accessDenied = false;

			while (!isDone) {
				
				if (accessDenied) {
					System.out.println("Access denied, or no such record exists! \n Type 'help' for commands.");
				}

				System.out.print(user.getUsername() + " commands>");
				msg = read.readLine();
				fetchRecords();
				splitMsg = msg.split("\\s+");

				try {
					if (msg.equalsIgnoreCase("quit")) {
						break;
					} else if (msg.equalsIgnoreCase("help")) {
						printHelp();
					} else if (splitMsg[0].equalsIgnoreCase("records")) {
						printRecords();
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("edit") && (accessDenied = hasPermissions(msg))) {
						editRecord(splitMsg[1]);
						fetchRecords();
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("read") && (accessDenied = hasPermissions(msg))) {
						printRecord(splitMsg[1]);
						accessDenied = false;
					} else if (splitMsg[0].equalsIgnoreCase("delete") && (accessDenied = hasPermissions(msg))) {
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
	
	/**
	 * Query an update of the client-sided records from server.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static void fetchRecords() throws ClassNotFoundException, IOException {
		out.println("recordfetch");
		out.flush();
		records = (ArrayList<Record>) ois.readObject();
	}

	/**
	 * Query the server to check if user has permission x.
	 * @param msg
	 * @return
	 * @throws IOException
	 */
	private static boolean hasPermissions(String msg) throws IOException {
		out.println(msg);
		out.flush();

		if (serverMsg.readLine().equalsIgnoreCase("granted")) {
			return true;
		}

		return false;
	}

	/**
	 * Sets client command line in an editing state and submits data to server
	 * unless it is interrupted by the user.
	 * @param rNbr
	 * @throws IOException
	 */
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

	/**
	 * Sets client command line in a Record creation state 
	 * before submitting to server, unless interrupted by user.
	 * @throws IOException
	 */
	private static void createRecord() throws IOException {
		boolean isDone = false;

		while (!isDone) {
			System.out.println("CREATE as <nurseName> <patientName> <division> <medicalData>: ");
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

	/**
	 * Prints somewhat formatted list of commands.
	 */
	private static void printHelp() {
		System.out.println("quit - Exits program.");
		System.out.println("records - This retrieves a list of available records.");
		System.out.println("read <record nbr> - Displays medical data specified record.");
		System.out.println("edit <record nbr> - Edit specified record.");
		System.out.println("delete <record nbr> - Delete specified record.");
		System.out.println("create - Enters record creation.");
	}

	/**
	 * Prints records associated to client user.
	 */
	private static void printRecords() {
		if (!records.isEmpty()){
			System.out.println("Associated records: ");

			for (Record r : records) {
				System.out.println(r.toString());
			}
		} else {
			System.out.println("No associated records.");
		}
	}

	/**
	 * Prints specified record.
	 * @param rNbr record ID
	 */
	private static void printRecord(String rNbr) {
		for (Record r : records) {
			if (r.getId() == Long.parseLong(rNbr) && r != null) {
				System.out.println(r.getMedicalData());
			}
		}
	}

	/**
	 * Function awaits a User object followed by 0 or more Record objects in an array.
	 * @return true if successful
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static boolean waitForLoginData() throws IOException, ClassNotFoundException {
		user = (User) ois.readObject();

		if (user != null) {
			System.out.println(user.toString());

			records = (ArrayList<Record>) ois.readObject();
		} else {
			return false;
		}
		
		printRecords();

		return true;
	}

}
