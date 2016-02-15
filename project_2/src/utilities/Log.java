package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

public class Log {
	private static final String logName = "action_log";
	private static File logFile;
	private static PrintWriter printWriter;
	
	public static void append(String info) {
		logFile = new File(logName);

		try {
			printWriter = new PrintWriter(new FileWriter(logFile, true));

			printWriter.print(Calendar.getInstance().getTime().toString() + "\t");
			printWriter.print(info + "\n");
			printWriter.flush();

			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}