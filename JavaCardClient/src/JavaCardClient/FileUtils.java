package JavaCardClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
	public static byte[] getFileContents(String path) throws IOException {
    	File file = new File(path);
    	byte[] bytesArray = new byte[(int) file.length()];
    	FileInputStream fis = new FileInputStream(file);
    	fis.read(bytesArray);
    	fis.close();
    	return bytesArray;
    }
}
