package utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	private static Logger logger;
	private FileHandler fh;
	
	public Log() {
		logger = Logger.getLogger("MyLog");

		try {
			fh = new FileHandler("/Users/Atlas/desktop/test1.log");
			logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
	        
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}	
	
	public static void logEventInfo(String info) {
		logger.info(info);
	}
}
