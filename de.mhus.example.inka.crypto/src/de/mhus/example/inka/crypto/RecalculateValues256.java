package de.mhus.example.inka.crypto;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;

public class RecalculateValues256 {

	final static String key =
			"-----BEGIN RSA PRIVATE KEY-----\n"+
			"MIGtAgEAAiEAxVq56rE81vq5AdHUW1A080fbJ9VMswMEQhq6eNZMeckCAwEAAQIh\n"+
			"AJPB8I5Zcm6WOuu02OQg8fKdgJTYP9r7BMLre6vaoJ5dAhEA85mCJpzJUAcM9t91\n"+
			"5QkVzwIRAM9mkZCsW9GtzhHmRiVIdOcCEQCm5WSjWcYfW0VJmt4mNmxHAhEAs9cA\n"+
			"yi5qv/qyAZtnn9SgaQIRAJNnH1i7zc7VZ4Zk0udBLLY=\n"+
			"-----END RSA PRIVATE KEY-----";

    public static void main(String[] args) throws Exception {
		String privKeyPEM = key.replace(
			"-----BEGIN RSA PRIVATE KEY-----\n", "")
			    .replace("-----END RSA PRIVATE KEY-----", "").replace("\n", "");

			// Base64 decode the data

			byte[] encodedPrivateKey = Base64.getDecoder().decode(privKeyPEM);

			try {
			    ASN1Sequence primitive = (ASN1Sequence) ASN1Sequence
			        .fromByteArray(encodedPrivateKey);
			    Enumeration<?> e = primitive.getObjects();
			    BigInteger v = ((ASN1Integer) e.nextElement()).getValue();

			    int version = v.intValue();
			    if (version != 0 && version != 1) {
			        throw new IllegalArgumentException("wrong version for RSA private key");
			    }
			    /**
			     * In fact only modulus and private exponent are in use.
			     */
			    BigInteger modulus = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger publicExponent = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger privateExponent = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger prime1 = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger prime2 = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger exponent1 = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger exponent2 = ((ASN1Integer) e.nextElement()).getValue();
			    BigInteger coefficient = ((ASN1Integer) e.nextElement()).getValue();
			    System.out.println("Modulus:     " + modulus.toString());
			    System.out.println("Exponent1:   " + exponent1.toString());
			    System.out.println("Exponent2:   " + exponent2.toString());
			    System.out.println("pubExpo:     " + publicExponent.toString());
			    System.out.println("privExpo:    " + privateExponent.toString());
			    System.out.println("prime1:      " + prime1.toString());
			    System.out.println("prime2:      " + prime2.toString());
			    System.out.println("Coefficient: " + coefficient.toString());
			    System.out.println();

			    
				// so kann man z auch ausrechnen, d.h. kleinster, maximaler z sind ...
				System.out.println("Z:    " + ( modulus.subtract(prime1).subtract(prime2).add(BigInteger.ONE)  ) );
				
				BigInteger Zmin = modulus.divide(BigInteger.valueOf(2)).subtract(BigInteger.ONE);
				BigInteger Zmax = modulus.subtract( 
						sqrt(new BigDecimal(modulus), 1).toBigInteger().multiply(BigInteger.valueOf(2)) )
						.add(BigInteger.ONE);
				System.out.println("Zmin: " + Zmin );
				System.out.println("Zmax: " + Zmax );
				// kleinster wert für d, da sonst kein mod möglich
//				System.out.println("Dmin: " + ( z.di / e ));
				
				System.out.println();
				BigInteger DZmin = computeDfromE(publicExponent, Zmin);
				BigInteger DZmax = computeDfromE(publicExponent, Zmax);
				// long DZcenter = ((DZmin - DZmax)/2 + DZmax) ;
				BigInteger DZcenter = DZmin.subtract(DZmax).divide(BigInteger.valueOf(2)).add(DZmax);
				BigInteger DZspread = DZmin.subtract(DZmax).abs();
				System.out.println("DZmin:    " + DZmin);
				System.out.println("DZmax:    " + DZmax);
				System.out.println("PrivKey:  " + privateExponent);
				System.out.println("DZspread: " + DZspread );
				
				System.out.println("DZcenter: " + DZcenter + " (" + (privateExponent.subtract(DZcenter)) + ")" );

			    
				System.out.println("Sqrt: " + Math.sqrt(16));
				System.out.println("Sqrt: " + sqrt(new BigDecimal(16), 1 ));
			    
			    
			} catch (IOException e2) {
			    throw new IllegalStateException();
//			} catch (NoSuchAlgorithmException e) {
//			    throw new IllegalStateException(e);
//			} catch (InvalidKeySpecException e) {
//			    throw new IllegalStateException(e);
			}
			
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

	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());
	
	
	public static BigDecimal sqrt(BigDecimal x, int scale)
    {
        // Check that x >= 0.
        if (x.signum() < 0) {
            throw new IllegalArgumentException("x < 0");
        }
 
        // n = x*(10^(2*scale))
        BigInteger n = x.movePointRight(scale << 1).toBigInteger();
 
        // The first approximation is the upper half of n.
        int bits = (n.bitLength() + 1) >> 1;
        BigInteger ix = n.shiftRight(bits);
        BigInteger ixPrev;
 
        // Loop until the approximations converge
        // (two successive approximations are equal after rounding).
        do {
            ixPrev = ix;
 
            // x = (x + n/x)/2
            ix = ix.add(n.divide(ix)).shiftRight(1);
 
            Thread.yield();
        } while (ix.compareTo(ixPrev) != 0);
 
        return new BigDecimal(ix, scale);
    }	
	

	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
	    BigDecimal fx = xn.pow(2).add(c.negate());
	    BigDecimal fpx = xn.multiply(new BigDecimal(2));
	    BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(),BigDecimal.ROUND_DOWN);
	    xn1 = xn.add(xn1.negate());
	    BigDecimal currentSquare = xn1.pow(2);
	    BigDecimal currentPrecision = currentSquare.subtract(c);
	    currentPrecision = currentPrecision.abs();
	    if (currentPrecision.compareTo(precision) <= -1){
	        return xn1;
	    }
	    return sqrtNewtonRaphson(c, xn1, precision);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	public static BigDecimal bigSqrt(BigDecimal c){
	    return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
	}	
	
}
