package JavaCardClient;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.ByteArrayOutputStream;
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
	};
	
	public byte[] receive(int len) throws IOException {
		byte buffer[] = new byte[len];
		in.read(buffer);
		return buffer;
	};
	
	// Reverse engineered signals for power up and down
	public void sendPowerUp() throws IOException {
		send(new byte[]{(byte)0xf0, 0x00, 0x00, (byte)0xf0});
	}
	
	public void sendPowerDown() throws IOException {
		send(new byte[]{(byte)0xe0, 0x00, 0x00, (byte)0xe0});
	}
	
	public void sendInstall()throws IOException {
		// install
		send(new byte[]{0x00, (byte)0xa4, 0x04, 0x00, 0x09, (byte)0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x08, 0x01, 0x7f});
		// create applet
		send(new byte[]{(byte)0x80, (byte)0xb8, 0x00, 0x00, 0xd, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00, 0x7f});
	}	
	public void sendSelectAesApplet() throws IOException {
		// select applet
		send(new byte[]{(byte)0x80, 0x00, 0x15, 0x00, 0x00, 0x11, // prefix
				0x00, (byte)0xA4, 0x04, 0x00, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x7F, // actual instruction
				(byte)0xc4, (byte)0x95}); // postfix
	}
	
	public void cleanup(){
		try {
			if(this.socket != null) socket.close();
			if(this.out != null) out.close();
            if(this.in != null) in.close();
		} catch(Exception e){
			LogUtils.log("AesClient", e.getStackTrace().toString());
		}
	}
}
