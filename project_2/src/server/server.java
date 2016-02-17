package server;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import utilities.Doctor;
import utilities.User;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;

	private ArrayList<User> users;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		users = new ArrayList<User>();
		users.add(new Doctor("test", "test", User.DIV_EMERGENCY, 1));
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			numConnectedClients++;
			System.out.println("client connected");
			System.out.println("client name (cert subject DN field): " + cert.getSubjectDN().getName());
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
			System.out.println("issuer name (cert issuer DN field): " + cert.getIssuerDN().getName());
			System.out.println("serial number (cert serial number field): " + cert.getSerialNumber().toString());

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {
				String[] splitMsg = clientMsg.split("\\s+");
				User login = null;

				for (String s : splitMsg) {
					System.out.println(s);
				}

				if (splitMsg[0].equalsIgnoreCase("login") && splitMsg.length == 3) {
					System.out.println("Login query.");
					for (User u : users) {
						login = u.login(splitMsg[1], splitMsg[2]);
					}
				}

				if (login != null)
					System.out.println("Sending: \n" + login.toString());
				else
					System.out.println("Sending: NULL");

				oos.writeObject(login);
				oos.flush();
				System.out.println("Sent object.");
			}

			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
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
