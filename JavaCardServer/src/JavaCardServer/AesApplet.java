package JavaCardServer;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.*;
import javacardx.crypto.*;

public class AesApplet extends Applet {
	//globals
    AESKey aesKey;
    Cipher aesCipher;
    RandomData random;
    static byte a[];
    final short dataOffset = (short) ISO7816.OFFSET_CDATA;

    //constructor
    private AesApplet (byte bArray[], short bOffset, byte bLength){
         aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
         aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
         a = new byte[ (short) 128];
         random.generateData(a, (short)0, (short)128);
         aesKey.setKey(a, (short) 0);
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
         if (buf[ISO7816.OFFSET_CLA] != 0) {
        	 ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
         }
         
         if (buf[ISO7816.OFFSET_INS] != (byte) (0xAA)) {
        	 ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
         }
         
         switch (buf[ISO7816.OFFSET_P1]){
         	 // Encrypt or decrypt the message
	         case (byte) 0x01:
	              doAES(apdu, Cipher.MODE_ENCRYPT);
	              return;
	         case (byte) 0x02:
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
    	 aesCipher.doFinal(buffer, (short) dataOffset, incomingLength, a, (short) (dataOffset + incomingLength));
         
         apdu.setOutgoing();
         apdu.setOutgoingLength((short) buffer.length);
         apdu.sendBytesLong(buffer, (short) dataOffset, (short)buffer.length);
    }
}