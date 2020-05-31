package JavaCardClient;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AesClient {
	
	private static final int PREFIX_LEN = 6;
	private static final int CLA = PREFIX_LEN + 0;
	private static final int INS = PREFIX_LEN + 1;
	private static final int P1 = PREFIX_LEN + 2;
	private static final int P2 = PREFIX_LEN + 3;
	private static final int AES_LEN_BYTES = 16;
	
	private Socket socket;
	private OutputStream out;
	private InputStream in;
	private String host;
	private int port;
	
    public AesClient(String host, int port) throws UnknownHostException, IOException {
    	this.host = host;
    	this.port = port;
    	this.socket = new Socket(this.host, this.port);
    	this.socket.setTcpNoDelay(true);
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
		receive(10);
	}
	
	public void sendPowerDown() throws IOException {
		LogUtils.log("AesClient.sendPowerDown", "powering down");
		send(new byte[]{(byte)0xe0, 0x00, 0x00, (byte)0xe0});
	}
	
	public void sendInstall()throws IOException {
		LogUtils.log("AesClient.sendInstall", "installing");
		// install
		send(new byte[]{
				(byte)0x80, 0x00, 0x13, 0x00, 0x00, 0x0f, // prefix
				0x00, (byte)0xa4, 0x04, 0x00, 0x09, (byte)0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x08, 0x01, 0x7f,
				0x10, (byte)0x93 //postfix 
				});
		receive(10);
		// create applet
		send(new byte[]{ 
				(byte)0x80, 0x00, 0x17, 0x00, 0x40, 0x13, // prefix
				(byte)0x80, (byte)0xb8, 0x00, 0x00, 0xd, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00, 0x7f,
				0x13, (byte)0x97 // postfix
				});
		receive(10);
	}
	
	public void sendSelectAesApplet() throws IOException {
		LogUtils.log("AesClient.sendSelectAesApplet", "selecting AES applet");
		// select applet
		send(new byte[]{(byte)0x80, 0x00, 0x15, 0x00, 0x00, 0x11, // prefix
				0x00, (byte)0xA4, 0x04, 0x00, 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x7F, // data
				(byte)0xc4, (byte)0x95}); // postfix
		receive(21);
	}
	
	public byte[] sendFileInChunks(String path, ClientAction action) throws IOException {
		LogUtils.log("AesClient.sendFile", "sending file");
		// TODO:: make this work properly
		byte[] contents = FileUtils.getFileContents(path);
		int paddingNeeded = contents.length % AES_LEN_BYTES;
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
		// P1 - 0x00 = encrypt, 0x01 = decrypt
    	// P2 - 0x00 = does not have more, 0x01 = has more
		byte[] prefix = {
				(byte)0x80, 0x00, 0x19, 0x00, 0x40, 0x15,
				0x00, // CLA
				(byte)0xaa, // INS
				0x00, // P1 
				0x00, // P2
				(byte)AES_LEN_BYTES // Le
		};
		byte[] postfix = { (byte)0xef, (byte)0x99 };
		
		// Tell it what we want to do
		if(action == ClientAction.ENCRYPT){
			prefix[P1] = 0x00;
		} else {
			prefix[P1] = 0x01;
		}
		
		// Figure out if we have more than one chunk to send
		if(paddedContents.length / AES_LEN_BYTES > 1){
			prefix[P2] = 0x01;
		}
		
		// Send file in 16 byte chunks
		for(int i = 0, j = 0; i < paddedContents.length; i += AES_LEN_BYTES, j++){
			byte[] apdu;
			byte[] contentSubArray = new byte[AES_LEN_BYTES];
			// If we are at last chunk we stop
			if(i == paddedContents.length - AES_LEN_BYTES){
				prefix[P2] = 0x00;
			}
			System.arraycopy(paddedContents, i, contentSubArray, 0, AES_LEN_BYTES);
			apdu = makeApdu(prefix, contentSubArray, postfix);
			LogUtils.log("AesClient.sendFileInChunks", LogUtils.byteArrayToHexStr(contentSubArray));
			send(apdu);
			receive(10);
			byte[] temp = receive(22);
			if(temp.length > 1){				
				System.arraycopy(temp, PREFIX_LEN, response, i, AES_LEN_BYTES);
			}
		}
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
