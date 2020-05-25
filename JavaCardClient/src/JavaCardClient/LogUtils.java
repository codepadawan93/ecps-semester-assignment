package JavaCardClient;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;  

public class LogUtils {
	private static final String LOG_FILE = ".syslog";
	
	private static String getDate(){
		 SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		 Date date = new Date();  
		 return formatter.format(date);  
	}
	
	public static String byteArrayToHexStr(byte[] arr){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++){
			sb.append(String.format("%02x ", arr[i]));
		}
		return sb.toString();
	}
	
	public static void log(String tag,  String log){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(LOG_FILE, true));
			writer.println(String.format("[%s]---[%s] --- %s", getDate(), tag, log));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(writer != null) writer.close();
		}
	}
}
