package JavaCardClient;

import java.io.IOException;
import java.net.UnknownHostException;
import JavaCardClient.AesClient;

public class AesMain {
	
	public static void main(String[] args) {
		// Data will be sent to localhost:9025
		AesClient client = null;
		try {
			// TODO:: Also read the response. For some reason it crashes
			client = new AesClient("127.0.0.1", 9025);
			client.sendPowerUp();
			client.sendInstall();
			client.sendSelectAesApplet();
			byte[] encryptedFile = client.sendFileInChunks("./TestData/testfile.txt", ClientAction.ENCRYPT);
			FileUtils.writeToFile("./Output/outfile.enc", encryptedFile);
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
