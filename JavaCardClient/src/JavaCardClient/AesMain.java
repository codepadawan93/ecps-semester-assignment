package JavaCardClient;

import java.io.IOException;
import java.net.UnknownHostException;

import JavaCardClient.AesClient;

public class AesMain {

	public static void main(String[] args) {
		// Data will be sent to localhost:9025
		AesClient client = new AesClient("127.0.0.1", 9025);
		try {
			client.sendFile("../testData/testfile.txt");
		} catch(UnknownHostException uhe){
			uhe.printStackTrace();
		} catch(IOException ioe){
			ioe.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			client.cleanup();
		}
	}

}
