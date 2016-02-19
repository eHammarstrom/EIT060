
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

import utilities.DBFileHandler;
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
			users = DBFileHandler.loadUsers();
			records = DBFileHandler.loadRecords();

			if (true) {

				String clientMsg = null;
				User loggedInUser = null;

				while (loggedInUser == null) {
					clientMsg = in.readLine();
					String[] splitMsg = clientMsg.split("\\s+");
					User login = null;

					if (splitMsg[0].equalsIgnoreCase("login") && splitMsg.length == 3) {

						for (User u : users) {
							login = u.login(splitMsg[1], splitMsg[2]);
							loggedInUser = u;
							if (login != null) {
								break;
							}
						}

						oos.writeObject(loggedInUser);
						oos.flush();
					}

					if (login != null)
						System.out.println("Sending: \n" + login.toString());
					else
						System.out.println("Sending: NULL");

					if (login != null && !records.isEmpty()) {

						ArrayList<Record> userRecords = new ArrayList<Record>();

						for (Record r : records) {
							if (login.isAssociated(r)) {
								userRecords.add(r);
							}
						}

						oos.writeObject(userRecords);
						oos.flush();
					}

					// oos.close();

				}

				System.out.println("AFTER LOGIN ATTEMPT!");

				String command = null;

				// in = new BufferedReader(new
				// InputStreamReader(socket.getInputStream()));

				while ((command = in.readLine()) != null) {
					String[] commandSplit = command.split("\\s+");

					for (String s : commandSplit) {
						System.out.println(s);
					}

					Record rec = null;

					for (Record r : records) {
						System.out.println(r.getId());
						if (Long.parseLong(commandSplit[1]) == r.getId()) {
							System.out.println("FOUND RECORD");
							rec = r;
							break;
						}
					}

					if (rec == null) {
						System.out.println("RECORD DOES NOT EXIST!");
						break;
					}

					boolean recordAccess = true;

					if (commandSplit[0].equalsIgnoreCase("read")) {
						System.out.println("READ");
						recordAccess = loggedInUser.readRecord(rec);
						returnAccess(recordAccess, printWriter);
					}

					if (commandSplit[0].equalsIgnoreCase("edit")) {
						System.out.println("EDIT");
						recordAccess = loggedInUser.writeRecord(rec);
						returnAccess(recordAccess, printWriter);
						
						if (recordAccess) {
							rec.write(in.readLine());
						}
					}

					if (commandSplit[0].equalsIgnoreCase("create")) {
						System.out.println("EDIT");
						recordAccess = loggedInUser.createRecord(rec);
						returnAccess(recordAccess, printWriter);
						
						if (recordAccess) {
							String[] recordData = in.readLine().split("\\s+");
						}
					}

					if (commandSplit[0].equalsIgnoreCase("delete")) {
						System.out.println("DELETE");
						recordAccess = loggedInUser.deleteRecord(rec);
						returnAccess(recordAccess, printWriter);
					}

					// oos.close();
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
