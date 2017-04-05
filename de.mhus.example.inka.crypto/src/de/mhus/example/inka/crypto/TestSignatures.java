package de.mhus.example.inka.crypto;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class TestSignatures {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, CertificateException, SignatureException {
        final KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA", "SUN");
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        gen.initialize(512, random);

        final KeyPair pair = gen.generateKeyPair();
        final PrivateKey privateKey = pair.getPrivate();
        Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
        sig.initSign(privateKey);

        final PublicKey publicKey = pair.getPublic();
        byte[] pubKey = publicKey.getEncoded();
        byte[] privKey = privateKey.getEncoded();
        
        System.out.println(publicKey);

        System.out.println("public key: " + publicKey.getFormat());
        System.out.println(new BigInteger(pubKey).toString(16));
        System.out.println("private key: " + privateKey.getFormat());
        System.out.println(new BigInteger(privKey).toString(16));

        
        String convertedPubKey = javax.xml.bind.DatatypeConverter.printBase64Binary(pubKey);
        System.out.println("Converted PubKey: " + convertedPubKey);
        
        byte[] msg = "Hello World!".getBytes();
        sig.update(msg);
        byte[] signature = sig.sign();
        System.out.println("Signature: " + new BigInteger(signature).toString(16));
        
        
        
	}

}
