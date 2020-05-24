package JavaCardClient;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;


public class AesClient {
	
	private Socket socket;
	private String host;
	private int port;
	
    public AesClient(String host, int port){
    	this.host = host;
    	this.port = port;
    }
    
	public void sendFile(String path) throws UnknownHostException, IOException {
		this.socket = new Socket(this.host, this.port);
		File file = new File(path);
        byte[] buffer = new byte[16 * 1024];
        InputStream in = new FileInputStream(file);
        OutputStream out = socket.getOutputStream();
        
        // Write APDU signal first, then the file
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }

        out.close();
        in.close();
        socket.close();
	};
	
	public void cleanup(){
		if(this.socket != null){
			try {
				socket.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
