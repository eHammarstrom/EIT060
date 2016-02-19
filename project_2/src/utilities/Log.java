package utilities;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

public class Log {
	public static final String EDIT = "EDIT";
	public static final String CREATE = "CREATE";
	public static final String READ = "READ";
	public static final String DELETE = "DELETE";

	private static final String LOG_NAME = "action_log";
	private static PrintWriter printWriter;
	
	public static void append(String info, String operation) {
		try {
			printWriter = new PrintWriter(new FileWriter(LOG_NAME, true));

			printWriter.print(Calendar.getInstance().getTime().toString() + "\t");
			printWriter.print(info + "\t" + operation + "\n");
			printWriter.flush();

			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}