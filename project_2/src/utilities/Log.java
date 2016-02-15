package utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public class Log {

	public static void main(String[] args){
		
	      JSONObject obj = new JSONObject();
	      
	      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	
	      obj.put("When", timeStamp);
	      obj.put("Who", "Doctor who");
	      obj.put("Where", "Division X");

	      System.out.print(obj);
	   }
	
	public void logEvent(String who, String where) {
		
	}
	
}
