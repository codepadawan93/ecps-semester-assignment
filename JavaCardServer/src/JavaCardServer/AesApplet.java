package JavaCardServer;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.*;
import javacardx.crypto.*;

public class AesApplet extends Applet {
	//globals
    private AESKey aesKey;
    private Cipher aesCipher;
    private byte acc[];
    private short dataOffset;

    //constructor
    private AesApplet (byte bArray[], short bOffset, byte bLength){
         register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    //install
    public static void install(byte bArray[], short bOffset, byte bLength){
         new AesApplet (bArray, bOffset, bLength);
    }

    public void process(APDU apdu){
    	byte[] buf = apdu.getBuffer();
	    if (selectingApplet()){
	    	return;
	    }
	    dataOffset = apdu.getOffsetCdata();
    	aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        acc = new byte[16];
        for(short i = 0; i < 16; i++){
        	acc[i] = buf[(short)(i + dataOffset)];
        }
        aesKey.setKey(acc, (short) 0);
	    if (buf[ISO7816.OFFSET_CLA] != 0x00) {
	    	ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
	    }
	    if (buf[ISO7816.OFFSET_INS] != (byte) (0xaa)) {
	    	ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
	    }
	     
	    switch (buf[ISO7816.OFFSET_P1]){
	        // Encrypt or decrypt the message
	    	case (byte) 0x01:
	            doAES(apdu, Cipher.MODE_ENCRYPT);
	    		ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
	            return;
	         case (byte) 0x02:
	        	ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
	            doAES(apdu, Cipher.MODE_DECRYPT);
	            return;
	         default:
	            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
	    }
    }

    private void doAES(APDU apdu, byte mode){
    	// TODO:: debug this
        byte buffer[] = apdu.getBuffer();
        short incomingLength = (short) (apdu.setIncomingAndReceive());
        
    	aesCipher.init(aesKey, mode);
    	aesCipher.doFinal(buffer, (short) dataOffset, incomingLength, acc, (short) (dataOffset + incomingLength));
         
        apdu.setOutgoing();
        apdu.setOutgoingLength((short) buffer.length);
        apdu.sendBytesLong(buffer, (short) dataOffset, (short)buffer.length);
    }
}