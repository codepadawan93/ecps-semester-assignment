package JavaCardServer;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.*;
import javacardx.crypto.*;

public class AesApplet extends Applet {
	private static final byte[] key = {
			0x74, 0x65, 0x73, 0x74, 0x70, 0x61, 0x73, 0x73,
			0x77, 0x6f, 0x72, 0x64, 0x31, 0x32, 0x33, 0x34 //testpassword1234
	};
	private static final byte[] iv = {
			0x04, 0x71, 0x58, 0x14, 0x20, 0x11, 0x0a, 0x1a,
			0x71, 0x1c, 0x5f, 0x1f, 0x41, 0x32, 0x11, 0x40 
	};
	
	private Cipher aesCipher;
	private AESKey aesKey;
	boolean processing;

    private AesApplet (){
    	aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
    	aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    	processing = false;
    	register();
    }

    //install
    public static void install(byte bArray[], short bOffset, byte bLength){
         new AesApplet ();
    }

    public void process(APDU apdu){
    	
    	if(selectingApplet()){
    		return;
    	}
    	
    	byte[] buffer = apdu.getBuffer();
    	short len = apdu.setIncomingAndReceive();
    	
	    if (buffer[ISO7816.OFFSET_CLA] != 0x00) {
	    	ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
	    }
	    if (buffer[ISO7816.OFFSET_INS] != (byte) (0xaa)) {
	    	ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
	    }
	    encryptDecrypt(apdu, len);
    }
    
    private void encryptDecrypt(APDU apdu, short len) {
    	// P1 - 0x00 = encrypt, 0x01 = decrypt
    	// P2 - 0x00 = does not have more, 0x01 = has more
    	if (len <= 0 || len % 16 != 0){
    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	}
    	
    	byte[] buffer = apdu.getBuffer();
    	byte mode = buffer[ISO7816.OFFSET_P1] == (byte)0x00 ? Cipher.MODE_ENCRYPT : Cipher.MODE_DECRYPT;
    	boolean hasMore = (buffer[ISO7816.OFFSET_P2] == (byte)0x01);
    	
    	if(!processing) {
    		aesKey.setKey(key, (short) 0);
        	aesCipher.init(aesKey, mode, iv, (short)0, (short)16);
        	processing = true;
    	}
    	
    	if(hasMore) {
    		aesCipher.update(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0);
    	} else {
    		aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0);
    		processing = false;
    	}
    	
    	apdu.setOutgoingAndSend((short)0, len);
    }
}