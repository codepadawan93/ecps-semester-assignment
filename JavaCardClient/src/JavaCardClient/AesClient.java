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
	
	private byte[] makeApdu(byte[] prefix, byte[] contents, byte[] postfix){
		byte[] apdu = new byte[prefix.length + contents.length  + postfix.length];
		// Concatenate to obtain the apdu
		for(int i = 0; i < prefix.length; i++){
			apdu[i] = prefix[i];
		}
		for(int i = 0; i < contents.length; i++){
			apdu[i + prefix.length] = contents[i];
		}
		for(int i = 0; i < postfix.length; i++){
			apdu[i + prefix.length + contents.length] = postfix[i];
		}
		return apdu;
	}
	
	private void updateBuffer(byte[] to, byte[] from, int indexTo, int indexFrom){
		for(int i = indexTo, j = indexFrom; i < to.length && j < from.length; i++, j++){
			to[i] = from[j];
		}
	}
	
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
	
	public byte[] sendFileInChunks(String path, ClientAction action) throws IOException {
		LogUtils.log("AesClient.sendFile", "sending file");
		// TODO:: make this work properly
		byte[] contents = FileUtils.getFileContents(path);
		int paddingNeeded = contents.length % 16;
		byte[] paddedContents = new byte[contents.length + paddingNeeded];
		// PKCS 7 padding
		for(int i = 0; i < paddedContents.length; i++){
			if(i < contents.length){
				paddedContents[i] = contents[i];
			} else {
				paddedContents[i] = (byte)paddingNeeded;
			}
		}
		byte[] response = new byte[paddedContents.length];
		// P1 0x01 - encrypt begin
		// P1 0x02 - decrypt begin
		// P1 0x03 - update
		// P1 0x04 - finalize
		byte[] prefix = {
				(byte)0x80, 0x00, 0x1d, 0x00, 0x00, 0x19, // Not sure why this envelope is needed
				0x00, // CLA
				(byte)0xaa, // INS
				0x00, // P1 
				0x00, // P2
				(byte)16 // Le
		};
		byte[] postfix = { (byte)0xbc, (byte)0x9d };
		
		if(action == ClientAction.ENCRYPT){
			prefix[8] = 0x01;
		} else {
			prefix[8] = 0x02;
		}
		
		// Send file in 16 byte chunks
		byte[] apdu;
		for(int i = 0; i < paddedContents.length; i+=16){
			if(i == 0){
				apdu = makeApdu(prefix, paddedContents, postfix);
				// send first 16 bytes and tell it we want to encrypt
				send(apdu);
				updateBuffer(response, receive(16), i, 0);
			} else {
				// set P1 to update
				prefix[8] = 0x03;
				apdu = makeApdu(prefix, paddedContents, postfix);
				send(apdu);
				updateBuffer(response, receive(16), i, 0);
			}
		}
		// finalize - just send the signal, no response required
		prefix[8] = 0x04;
		prefix[10] = 0x00;
		apdu = makeApdu(prefix, new byte[]{}, new byte[]{});
		send(apdu);
		return response;
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
