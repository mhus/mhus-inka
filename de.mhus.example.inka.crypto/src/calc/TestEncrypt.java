package calc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// http://www.macs.hw.ac.uk/~ml355/lore/pkencryption.htm

/*

Usage

To use the code, you need corresponding public and private RSA keys. RSA keys can be generated using the open source tool OpenSSL. However, you have to be careful to generate them in the format required by the Java encryption libraries. To generate a private key of length 2048 bits:

openssl genrsa -out private.pem 2048
To get it into the required (PKCS#8, DER) format:

openssl pkcs8 -topk8 -in private.pem -outform DER -out private.der -nocrypt
To generate a public key from the private key:

openssl rsa -in private.pem -pubout -outform DER -out public.der
An example of how to use the code:

FileEncryption secure = new FileEncryption();

// to encrypt a file
secure.makeKey();
secure.saveKey(encryptedKeyFile, publicKeyFile);
secure.encrypt(fileToEncrypt, encryptedFile);

// to decrypt it again
secure.loadKey(encryptedKeyFile, privateKeyFile);
secure.decrypt(encryptedFile, unencryptedFile);

 */
public class TestEncrypt {

	private static final int AES_Key_Size = 256;

	public static void main(String[] args) throws GeneralSecurityException, IOException {

		String content = "Hello World!";
		
		TestEncrypt inst = new TestEncrypt();
		inst.FileEncryption();
		
		// encrypt 
		
		inst.makeKey();
		inst.saveKey(new File("private.key"), new File("private.der"));

		ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		inst.encrypt(is, os);
		
		byte[] encoded = os.toByteArray();

		// decrypt 
		
		inst.loadKey(new File("public.key"), new File("public.der"));

		
		is = new ByteArrayInputStream(encoded);
		os = new ByteArrayOutputStream();
		inst.decrypt(is, os);
		
		byte[] decoded = os.toByteArray();
		
		System.out.println("Result: " + new String(decoded));
	}

	private Cipher pkCipher;
	private Cipher aesCipher;
	private byte[] aesKey;
	private SecretKeySpec aeskeySpec;
	
	public void FileEncryption() throws GeneralSecurityException {
	    // create RSA public key cipher
	    pkCipher = Cipher.getInstance("RSA");
	    // create AES shared key cipher
	    aesCipher = Cipher.getInstance("AES");
	  }

	public void makeKey() throws NoSuchAlgorithmException {
	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(AES_Key_Size);
	    SecretKey key = kgen.generateKey();
	    aesKey = key.getEncoded();
	    aeskeySpec = new SecretKeySpec(aesKey, "AES");
	  }
	
	public void encrypt(InputStream is, OutputStream out) throws IOException, InvalidKeyException {
	    aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
	    
	    CipherOutputStream os = new CipherOutputStream(out, aesCipher);
	    
	    copy(is, os);
	    
	    os.close();
	  }
	
	public void decrypt(InputStream in, OutputStream os) throws IOException, InvalidKeyException {
	    aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
	    
	    CipherInputStream is = new CipherInputStream(in, aesCipher);
	    
	    copy(is, os);
	    
	    is.close();
	    os.close();
	  }
	  
	  private void copy(InputStream is, OutputStream os) throws IOException {
	    int i;
	    byte[] b = new byte[1024];
	    while((i=is.read(b))!=-1) {
	      os.write(b, 0, i);
	    }	
	  }

	  public void saveKey(File out, File publicKeyFile) throws IOException, GeneralSecurityException {
		    // read public key to be used to encrypt the AES key
		    byte[] encodedKey = new byte[(int)publicKeyFile.length()];
		    new FileInputStream(publicKeyFile).read(encodedKey);
		    
		    // create public key
		    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    PublicKey pk = kf.generatePublic(publicKeySpec);
		    
		    // write AES key
		    pkCipher.init(Cipher.ENCRYPT_MODE, pk);
		    CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), pkCipher);
		    os.write(aesKey);
		    os.close();
		  }	  
	  
	  public void loadKey(File in, File privateKeyFile) throws GeneralSecurityException, IOException {
		    // read private key to be used to decrypt the AES key
		    byte[] encodedKey = new byte[(int)privateKeyFile.length()];
		    new FileInputStream(privateKeyFile).read(encodedKey);
		    
		    // create private key
		    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    PrivateKey pk = kf.generatePrivate(privateKeySpec);
		    
		    // read AES key
		    pkCipher.init(Cipher.DECRYPT_MODE, pk);
		    aesKey = new byte[AES_Key_Size/8];
		    CipherInputStream is = new CipherInputStream(new FileInputStream(in), pkCipher);
		    is.read(aesKey);
		    aeskeySpec = new SecretKeySpec(aesKey, "AES");
		  } 	  
}
