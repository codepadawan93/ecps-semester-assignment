package JavaCardClient;

import java.io.IOException;
import java.net.UnknownHostException;
import JavaCardClient.AesClient;

public class AesMain {

	public static void main(String[] args) {
		// Data will be sent to localhost:9025
		AesClient client = null;
		try {
			client = new AesClient("127.0.0.1", 9025);
			client.sendPowerUp();
			client.sendSelectAesApplet();
			byte[] res = client.receive(10);
			int len = client.sendFile("./TestData/testfile.txt");
			// res = client.receive(len);
			client.sendPowerDown();
		} catch(UnknownHostException uhe){
			uhe.printStackTrace();
			LogUtils.log("AesMain.main", uhe.getMessage());
		} catch(IOException ioe){
			ioe.printStackTrace();
			LogUtils.log("AesMain.main", ioe.getMessage());
		} catch(Exception e){
			e.printStackTrace();
			LogUtils.log("AesMain.main", e.getMessage());
		} finally {
			client.cleanup();
		}
	}
}
