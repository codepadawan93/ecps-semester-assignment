package JCServer;

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
         if (selectingApplet())
         {
              return;
         }
         if (buf[ISO7816.OFFSET_CLA] != 0) ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
         
         if (buf[ISO7816.OFFSET_INS] != (byte) (0xAA)) ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
         
         switch (buf[ISO7816.OFFSET_P1])
         {
         case (byte) 0x01:
              doAES(apdu);
              return;
         default:
              ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
         }
    }

    private void doAES(APDU apdu){
         
         byte b[] = apdu.getBuffer();
         
         short incomingLength = (short) (apdu.setIncomingAndReceive());
         if (incomingLength != 24) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

         //perform encryption and append results in APDU Buffer a[] automatically 
         
         aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
         aesCipher.doFinal(b, (short) dataOffset, incomingLength, a, (short) (dataOffset + 24));
         aesCipher.init(aesKey, Cipher.MODE_DECRYPT);
         aesCipher.doFinal(b, (short) (dataOffset + 24), incomingLength, a, (short) (dataOffset + 48));

         // Send results
         apdu.setOutgoing();
         apdu.setOutgoingLength((short) 72);
         apdu.sendBytesLong(b, (short) dataOffset, (short) 72);
    }
}