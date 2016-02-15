package utilities;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

public class Log {
	private static final String LOG_NAME = "action_log";
	private static PrintWriter printWriter;
	
	public static void append(String info) {
		try {
			printWriter = new PrintWriter(new FileWriter(LOG_NAME, true));

			printWriter.print(Calendar.getInstance().getTime().toString() + "\t");
			printWriter.print(info + "\n");
			printWriter.flush();

			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}