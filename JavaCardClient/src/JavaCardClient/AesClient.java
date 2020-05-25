package JavaCardClient;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AesClient {
	
	private Socket socket;
	private OutputStream out;
	private InputStream in;
	private String host;
	private int port;
	
    public AesClient(String host, int port) throws UnknownHostException, IOException {
    	this.host = host;
    	this.port = port;
    	this.socket = new Socket(this.host, this.port);
    	out = socket.getOutputStream();
    	in = socket.getInputStream();
    }
    
	private void send(byte[] arr) throws IOException {
        out.write(arr, 0, arr.length);
        LogUtils.log("AesClient.send", LogUtils.byteArrayToHexStr(arr));
	};
	
	public byte[] receive(int len) throws IOException {
		byte buffer[] = new byte[len];
		int read = in.read(buffer);
		LogUtils.log("AesClient.receive", LogUtils.byteArrayToHexStr(buffer));
		if(read < 0){
			return new byte[]{};
		}
		return buffer;
	};
	
	// Reverse engineered signals for power up and down
	public void sendPowerUp() throws IOException {
		LogUtils.log("AesClient.sendPowerUp", "powering up");
		send(new byte[]{(byte)0xf0, 0x00, 0x00, (byte)0xf0});
	}
	
	public void sendPowerDown() throws IOException {
		LogUtils.log("AesClient.sendPowerDown", "powering down");
		send(new byte[]{(byte)0xe0, 0x00, 0x00, (byte)0xe0});
	}
	
	public void sendInstall()throws IOException {
		LogUtils.log("AesClient.sendInstall", "installing");
		// install
		send(new byte[]{0x00, (byte)0xa4, 0x04, 0x00, 0x09, (byte)0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x08, 0x01, 0x7f});
		// create applet
		send(new byte[]{(byte)0x80, (byte)0xb8, 0x00, 0x00, 0xd, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00, 0x7f});
	}
	
	public void sendSelectAesApplet() throws IOException {
		LogUtils.log("AesClient.sendSelectAesApplet", "selecting AES applet");
		// select applet
		send(new byte[]{(byte)0x80, 0x00, 0x15, 0x00, 0x00, 0x11, // prefix
				0x00, (byte)0xA4, 0x04, 0x00, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x7F, // data
				(byte)0xc4, (byte)0x95}); // postfix
	}
	
	public int sendFile(String path) throws IOException {
		LogUtils.log("AesClient.sendFile", "sending file");
		// TODO:: make this work properly
		byte[] contents = FileUtils.getFileContents(path);
		byte[] key = {
				0x74, 0x65, 0x73, 0x74, 0x70, 0x61, 0x73, 0x73,
				0x77, 0x6f, 0x72, 0x64, 0x31, 0x32, 0x33, 0x34 // testpassword1234
		};
		byte[] prefix = {
				(byte)0x80, 0x00, 0x1d, 0x00, 0x00, 0x19, // Not sure whyuu this envelope is needed
				0x00, // CLA
				(byte)0xaa, // INS
				0x01, 0x01, // P1/P2
				(byte)(key.length + contents.length) // Le
		};
		byte[] postfix = { (byte)0xbc, (byte)0x9d };
		byte[] apdu = new byte[prefix.length + key.length + contents.length  + postfix.length];
		// Concatenate to obtain the apdu
		for(int i = 0; i < prefix.length; i++){
			apdu[i] = prefix[i];
		}
		for(int i = 0; i < key.length; i++){
			apdu[i + prefix.length] = key[i];
		}
		for(int i = 0; i < contents.length; i++){
			apdu[i + key.length + prefix.length] = contents[i];
		}
		for(int i = 0; i < postfix.length; i++){
			apdu[i + key.length + prefix.length + contents.length] = postfix[i];
		}
		send(apdu);
		return apdu.length;
	}
	
	public void cleanup(){
		LogUtils.log("AesClient.cleanup", "cleaning up");
		try {
			if(this.socket != null) socket.close();
			if(this.out != null) out.close();
            if(this.in != null) in.close();
		} catch(Exception e){
			LogUtils.log("AesClient.cleanup", e.getStackTrace().toString());
		}
	}
}
