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

			BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

			User user;
			ArrayList<Record> records = new ArrayList<Record>();
			String msg;
			for (;;) {
				System.out.print(">");

				msg = read.readLine();

				if (msg.equalsIgnoreCase("quit")) {
					break;
				}

				out.println(msg);
				out.flush();

				user = (User) ois.readObject();
				
				if (user != null) {
					System.out.println(user.toString());

					for (;;) {
						try {
							records.add((Record) ois.readObject());
						} catch (Exception e) {
							ois.close();
							break;
						}
					}
				} else {
					System.out.println("obj is NULL");
				}
				
				System.out.println("We left.");
				
				if (!records.isEmpty()) {
					for (Record r : records) {
						System.out.println(r.toString());
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

}
