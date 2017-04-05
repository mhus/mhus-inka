package de.mhus.example.inka.crypto;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MTimeInterval;

// openssl genrsa -out private256.pem 256

public class ExtractPemAndEncode {

	final static String key2048 =
	"-----BEGIN RSA PRIVATE KEY-----\n"+
	"MIIEowIBAAKCAQEAoUyyHPciq+UhB8CEb/1YIeO7/hmbQL3kxaxHRFWZzZVjsD09\n"+
	"KPqwHwHJ9xpBGg7K+4K8nuEVR2SrCO8KNNqhqqLkIjO1v9kn65grflfeP0MdRmZu\n"+
	"58FpXiurb0yapwJVqCynTnXK6yUmgAMWRC3SvXnChsr0U8lLfkvsQ3cTAhrR0z1L\n"+
	"/R02d2geZnBj+mu1fMomjVccEbHnmOL2+/PkkhtUkQiClKPz63w7xd9fvF8cFnhV\n"+
	"E3FqUHEu6J2G2cnhN86C8U4eU1kb0eWCnSzzGVLtUsIf1tlt5TaNyWDy5RdYw49s\n"+
	"kqoqKvmXEhcc65oBphDLgFg1FfypqO1ojUXhBQIDAQABAoIBAAUDckHOOKipHYa1\n"+
	"KCim8jdTccNrHlU70cGHIkvwcTBfpVKUBLOiXxkHoDRq/30E2rBIlv5FNrkaWuqT\n"+
	"K3kLFp1MJNUfUFXfNQtwlmF97619s4o9otLXQyQnLVPvSJtKSklI4gZhSOZYKEMw\n"+
	"VV/XIMa84xv3cPKtvgf16ikKqW+WQy97IKGMLhbrEhbKDdmY5mcvpLQ0OM2Btmjp\n"+
	"rgs5SEavWoNrIzHqgQ6xNB13oJxPtDCfWo1GkHFpsGSp5jzvsQQHZMVMGxera6+l\n"+
	"kq5EhK2XA3r7zf3b9dGCYUNqU4HNd2WU3AbTOi1omaPkKITeJ78U2wmc/QDb3S3c\n"+
	"dYq7fF0CgYEAy6pINWueYqES4OBq4fv+g6D9zVD9eEE05z1yj7Bay+Fa/kvx5E4m\n"+
	"Y8nWJ/EDOBBOCJRq1Ebshr8UEuJIDbR8D0E8N63+L0nOqLppj6L/eEtajXVlqf5J\n"+
	"pb0+Oo/jW6sn4m4guvrPLn36r1bzXiFp9wKZjUVIqlGwIiz8XxhyjgcCgYEAyr96\n"+
	"CcpTAzRRV+7RBV446fYvJJjbNcHYYKvcHMg6BfgOOwbsUHnNvzwiJJZujntkFZp9\n"+
	"tYDZIGW85fCuVnrlxGzn52PgqZE0kZ9OaeLG74sGMJx6H2x8uEMXRQyM7s48OYBB\n"+
	"X9GCIWyNah6zFsJPomyYiDHB7E9P6u14tx16VZMCgYEAwJiD9niR6+U0bBHtIU1i\n"+
	"7ukUec+IEute8vnp1zXHdxviJ657zhGVPjKFYXoKOD86++QWbi2vyPDzM7RmvQcb\n"+
	"dnWTU3gncmKSmn7GCn3yprhjpngJLst4q9IdAdZGA88ERZ0tOISr3eRmZt+L/00L\n"+
	"3vnHaY/GWsIrFPaDpg4BbosCgYAz2wlhm6fjt+veK6y2TMUNwfOIzreyZiPrhclE\n"+
	"a0m74RfyrPCgHKcs9DpfVUJtms2cYOkqFQxzptHLleVhJQnDVX9yxS7e786cODyc\n"+
	"BG6RMeOhZ0Qs6Vh04GQBOxaItaLdqhoOYc2AsvzwWW3Asm4fwtq4atGImTh9g8NO\n"+
	"QnHZlQKBgGcp17bbPD43khFDRvcyHwtguMrKdGxMav+c+jEUtVjsY6meb+EWnUwM\n"+
	"VU2hmZcHNJacp5XQd/GyWqqGkzm9kMV4z5hfiIElhgRg2h3fkpUoVoytNlL4p5kU\n"+
	"btvEfCueGxAFk2OtpGn5akKIpITtF/lVxbTN0yFqhEuaL+pSubW5\n"+
	"-----END RSA PRIVATE KEY-----";
	
	final static String key256 =
			"-----BEGIN RSA PRIVATE KEY-----\n"+
			"MIGtAgEAAiEAxVq56rE81vq5AdHUW1A080fbJ9VMswMEQhq6eNZMeckCAwEAAQIh\n"+
			"AJPB8I5Zcm6WOuu02OQg8fKdgJTYP9r7BMLre6vaoJ5dAhEA85mCJpzJUAcM9t91\n"+
			"5QkVzwIRAM9mkZCsW9GtzhHmRiVIdOcCEQCm5WSjWcYfW0VJmt4mNmxHAhEAs9cA\n"+
			"yi5qv/qyAZtnn9SgaQIRAJNnH1i7zc7VZ4Zk0udBLLY=\n"+
			"-----END RSA PRIVATE KEY-----";

	final static String key = key256;
	
    public static void main(String[] args) throws Exception {
    	{
		String privKeyPEM = key.replace(
			"-----BEGIN RSA PRIVATE KEY-----\n", "")
			    .replace("-----END RSA PRIVATE KEY-----", "").replace("\n", "");

			// Base64 decode the data

			byte[] encodedPrivateKey = Base64.getDecoder().decode(privKeyPEM);

			try {
			    ASN1Sequence primitive = (ASN1Sequence) ASN1Sequence
			        .fromByteArray(encodedPrivateKey);
			    Enumeration<?> ex = primitive.getObjects();
			    BigInteger v = ((ASN1Integer) ex.nextElement()).getValue();

			    int version = v.intValue();
			    if (version != 0 && version != 1) {
			        throw new IllegalArgumentException("wrong version for RSA private key");
			    }
			    /**
			     * In fact only modulus and private exponent are in use.
			     */
			    BigInteger modulus = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger publicExponent = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger privateExponent = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger prime1 = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger prime2 = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger exponent1 = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger exponent2 = ((ASN1Integer) ex.nextElement()).getValue();
			    BigInteger coefficient = ((ASN1Integer) ex.nextElement()).getValue();

//			    RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExponent);
//			    KeyFactory kf = KeyFactory.getInstance("RSA");
//			    PrivateKey pk = kf.generatePrivate(spec);
			    
			    System.out.println("Values from Key File:");
			    System.out.println(" Modulus:     " + modulus.toString());
			    System.out.println(" Exponent1:   " + exponent1.toString());
			    System.out.println(" Exponent2:   " + exponent2.toString());
			    System.out.println(" pubExpo:     " + publicExponent.toString());
			    System.out.println(" privExpo:    " + privateExponent.toString());
			    System.out.println(" prime1:      " + prime1.toString());
			    System.out.println(" prime2:      " + prime2.toString());
			    System.out.println(" Coefficient: " + coefficient.toString());
			    System.out.println();
			    
			    
			    
			    
			    System.out.println("Recalculate:");
			    {
			    // (D * E) % z = 1
			    BigInteger n = prime1.multiply(prime2);
			    System.out.println(" Modulus:     " + n);
			    BigInteger z = prime1.subtract(BigInteger.ONE).multiply(  prime2.subtract(BigInteger.ONE)   );
			    System.out.println(" Z            " + z );
			    BigInteger e = computeDfromE(privateExponent, z);
			    System.out.println(" E from D:    " + e );
			    BigInteger d = computeDfromE(publicExponent, z);
			    System.out.println(" D from E:    " + d );
			    System.out.println(" Validate cal:" + e.multiply(d).mod(z) );
			    System.out.println(" Validate org:" + publicExponent.multiply(privateExponent).mod(z) );
			    System.out.println();
			    }
			    
			    
			    
			    
			    
			    System.out.println("Expo Modulo:");
			    {
			    	BigInteger base = BigInteger.valueOf(32384);
			    	BigInteger exp = BigInteger.valueOf(349);
			    	BigInteger mod = BigInteger.valueOf(893458);
			    	
			    	System.out.println(" Function: " + base + "^" + exp + " mod " + mod );
				    System.out.println(" Java:     " + base.pow(exp.intValue()).mod( mod ) );
				    System.out.println(" Staright: " + straightPow( base, exp, mod ) );
				    System.out.println(" Divide:   " + dividePow  ( base, exp, mod ) );
				    System.out.println(" Split:    " + splitPow   ( base, exp, mod ) );
				    System.out.println(" Binary:   " + binaryPow  ( base, exp, mod ) );
			    }
			    System.out.println();
			    System.out.println("Encode / Decode:");
			    // encode
			    byte[] content = "H".getBytes();
			    BigInteger c = new BigInteger(content);
			    
			    System.out.println(" Original: " + c);

			    BigInteger encoded = binaryPow(c, publicExponent, modulus);
			    
			    System.out.println(" Encoded: " + encoded.toString(16));
			    
			    BigInteger decoded2 = binaryPow(encoded, privateExponent, modulus);
			    
			    System.out.println(" Decoded: " + decoded2);
			    
			    System.out.println();
			    System.out.println("Fries out of the bottle:");
			    BigInteger x = findFries(encoded, decoded2, modulus);
			    System.out.println(" Fries: " + x);
			    
			} catch (IOException e2) {
			    throw new IllegalStateException();
//			} catch (NoSuchAlgorithmException e) {
//			    throw new IllegalStateException(e);
//			} catch (InvalidKeySpecException e) {
//			    throw new IllegalStateException(e);
			}
			
		}	
/*    	
		{
			// Remove the first and last lines
	
	        String privKeyPEM = key.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
	        privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
	        privKeyPEM = privKeyPEM.replace("\n","");
	        System.out.println(privKeyPEM);
	
	        // Base64 decode the data
	
	        byte [] encoded = Base64.getDecoder().decode(privKeyPEM);
	
	        // PKCS8 decode the encoded RSA private key
	
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        PrivateKey privKey = kf.generatePrivate(keySpec);
	
	        // Display the results
	
	        System.out.println(privKey);
		}
*/		
    }
    
    static BigInteger TWO = BigInteger.valueOf(2);
        
    private static BigInteger dividePow( BigInteger base, BigInteger pow, BigInteger mod) {
    	System.err.print("  ");
    	return dividePow(base, pow, 1).mod(mod);
    }
    
    private static BigInteger dividePow( BigInteger base, BigInteger pow, long level ) {
    	System.err.print(level + " ");
    	boolean odd = false;
    	if (pow.mod(TWO).equals(BigInteger.ONE)) {
    		// odd
    		pow = pow.subtract(BigInteger.ONE);
    		odd = true;
    	}
    	
    	BigInteger half = pow.divide(TWO);
    	
    	BigInteger res = null;
    	if (half.equals(BigInteger.ONE)) {
    		System.err.println();
    		res = base.multiply(base);
    	} else {
    		res = dividePow(base, half, level+1);
    		res = res.multiply(res);
    	}
    	if (odd)
    		res = res.multiply(base);
    	
    	System.err.println("  " + level + ": " + res.bitCount());
    	return res;
    }
    
    static BigInteger straightPow(BigInteger base, BigInteger pow, BigInteger mod) {
    	BigInteger res = base;
//	    BigInteger lastp = BigInteger.ZERO;
//	    BigInteger onep = exponent1.divide(BigInteger.valueOf(100));
	    BigInteger n1000 = BigInteger.valueOf(1000);
	    for (BigInteger bi = BigInteger.ONE; bi.compareTo( pow ) == -1; bi = bi.add(BigInteger.ONE)) {
//	    	BigInteger p = bi.divide( onep );
//	    	if (!p.equals(lastp)) {
//	    		System.out.println(p + " %");
//	    		lastp = p;
//	    	}
	    	if (bi.mod(n1000).equals(BigInteger.ZERO)) {
	    		System.err.println("  " + bi + " " + base.bitLength() );
	    	}
	    	res = res.multiply(base);
        }
	    return res.mod(mod);
    }

    static BigInteger splitPow(BigInteger base, BigInteger pow, BigInteger mod) {
    	BigInteger res = base;
//	    BigInteger lastp = BigInteger.ZERO;
//	    BigInteger onep = exponent1.divide(BigInteger.valueOf(100));
//	    BigInteger n1000 = BigInteger.valueOf(100000000l);
    	int cnt = 0;
    	long start = System.currentTimeMillis();
    	long lastStart = start;
	    for (BigInteger bi = BigInteger.ONE; bi.compareTo( pow ) == -1; bi = bi.add(BigInteger.ONE)) {
	    	cnt++;
	    	if (cnt >= 100000000) {
	    		System.err.println("  " + bi + " " + base.bitLength() + " left " + (pow.subtract(bi)) );
	    		long cur = System.currentTimeMillis();
	    		BigInteger estimated = BigInteger.valueOf(cur - start).multiply( pow.subtract(bi) ).divide(bi);
	    		long esti = estimated.longValue();
	    		System.err.println( "    " + getCurrentTimeAsString(esti) + " " + ( 100000000 / ((cur - lastStart)/1000) ) );
	    		cnt = 0;
	    		lastStart = cur;
	    	}
	    	res = res.multiply(base).mod(mod);
        }
	    return res.mod(mod);
    }

	public static String getCurrentTimeAsString(long msec) {
		long sec = msec / 1000;
		long min = sec / 60;
		long hours = min / 60;
		long days = hours / 24;
		return MCast.toString((int) (days), 2) + ' '
				+ MCast.toString((int) (hours % 24), 2) + ':'
				+ MCast.toString((int) (min % 60), 2) + ':'
				+ MCast.toString((int) (sec % 60), 2) + '.'
				+ MCast.toString((int) (msec % 1000), 3);

	}
	
	static BigInteger binaryPow(BigInteger base, BigInteger pow, BigInteger mod) throws IOException {
		
		if ( mod.subtract(BigInteger.ONE).pow(2).compareTo(base) > 1 ) {
			throw new IOException("modulo is to big");
		}
		BigInteger res = BigInteger.ONE;
		base = base.mod(mod);
		
		while (pow.compareTo(BigInteger.ZERO) == 1 ) {
			if (pow.mod(TWO).equals(BigInteger.ONE))
				res = res.multiply(base).mod(mod);
			pow = pow.shiftRight(1);
			base = base.multiply(base).mod(mod);
		}
		return res;
	}

	//http://everything2.com/index.pl?node_id=946812        
	public static BigDecimal log10(BigDecimal b, int dp)
	{
		final int NUM_OF_DIGITS = dp+2; // need to add one to get the right number of dp
		                                //  and then add one again to get the next number
		                                //  so I can round it correctly.

		MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);

		//special conditions:
		// log(-x) -> exception
		// log(1) == 0 exactly;
		// log of a number lessthan one = -log(1/x)
		if(b.signum() <= 0)
			throw new ArithmeticException("log of a negative number! (or zero)");
		else if(b.compareTo(BigDecimal.ONE) == 0)
			return BigDecimal.ZERO;
		else if(b.compareTo(BigDecimal.ONE) < 0)
			return (log10((BigDecimal.ONE).divide(b,mc),dp)).negate();

		StringBuffer sb = new StringBuffer();
		//number of digits on the left of the decimal point
		int leftDigits = b.precision() - b.scale();

		//so, the first digits of the log10 are:
		sb.append(leftDigits - 1).append(".");

		//this is the algorithm outlined in the webpage
		int n = 0;
		while(n < NUM_OF_DIGITS)
		{
			b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
			leftDigits = b.precision() - b.scale();
			sb.append(leftDigits - 1);
			n++;
		}

		BigDecimal ans = new BigDecimal(sb.toString());

		//Round the number to the correct number of decimal places.
		ans = ans.round(new MathContext(ans.precision() - ans.scale() + dp, RoundingMode.HALF_EVEN));
		return ans;
	}
	
	// using baghdad method
	static BigInteger computeDfromE(BigInteger e, BigInteger z) {
		BigDecimal E = new BigDecimal(e);
		BigDecimal Z = new BigDecimal(z);
		BigDecimal D = new BigDecimal(1);
		BigDecimal T = null;
		do {
			D = D.add(Z);
			T = D.divide(E, 100, BigDecimal.ROUND_UP).stripTrailingZeros();
		} while ( T.scale() > 0);
		return T.toBigInteger();
	}

	static BigInteger findFries(BigInteger start, BigInteger e, BigInteger z) {
		BigDecimal E = new BigDecimal(e);
		BigDecimal Z = new BigDecimal(z);
		BigDecimal D = new BigDecimal(start);
		BigDecimal T = null;
		BigDecimal logE = log10(E, 10);
		do {
			D = D.add(Z);
			T = D.divide(E, 100, BigDecimal.ROUND_UP).stripTrailingZeros();
			if (T.scale() > 0) {
				// is it an exponent?
				BigDecimal exp = log10(T, 10).divide( logE, 100, BigDecimal.ROUND_UP ).stripTrailingZeros();
				//System.err.println(exp);
				if (exp.scale() <= 0) {
					return exp.add(BigDecimal.ONE).toBigInteger();
				}
			}
		} while (true);
	}
	
	
}
