package utilities;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

public class Log {
	public static final String LOGIN = "LOGIN";

	private static final String LOG_NAME = "action_log";
	private static PrintWriter printWriter;
	
	public static void append(String info, String operation, Boolean status) {
		try {
			printWriter = new PrintWriter(new FileWriter(LOG_NAME, true));

			printWriter.print(Calendar.getInstance().getTime().toString() + "\t");
			printWriter.print(info + "\t" + operation + "\t ACCESS: " + status + "\n");
			printWriter.flush();

			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}