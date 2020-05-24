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
			LogUtils.log("AesMain", LogUtils.byteArrayToHexStr(res));
			// TODO :: Perform encryption
			client.sendPowerDown();
		} catch(UnknownHostException uhe){
			uhe.printStackTrace();
			LogUtils.log("AesMain", uhe.getMessage());
		} catch(IOException ioe){
			ioe.printStackTrace();
			LogUtils.log("AesMain", ioe.getMessage());
		} catch(Exception e){
			e.printStackTrace();
			LogUtils.log("AesMain", e.getMessage());
		} finally {
			client.cleanup();
		}
	}
}
