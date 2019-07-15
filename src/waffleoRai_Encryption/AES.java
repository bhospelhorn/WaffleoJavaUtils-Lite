package waffleoRai_Encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	//Yeah, I don't feel like debugging it.
	//Let's see what the Java libraries have...
	//https://www.novixys.com/blog/java-aes-example/
	
	/* ----- Constants ----- */
	
	public static final String CIPHER_TRANSFORMATION = "AES/CBC/NoPadding";
	public static final String CIPHER_TRANSFORMATION_PADDING = "AES/CBC/PKCS5Padding";
	
	/* ----- Static Variables ----- */
	
	/* ----- Instance Variables ----- */
	
	private byte[] aes_key;
	
	/* ----- Construction ----- */
	
	public AES(byte[] key)
	{
		aes_key = key;
	}
	
	/**
	 * Construct an AES encrytor/decryptor with the provided AES key
	 * as an array of bytes 0-extended to ints.
	 * <br><b>!IMPORTANT!</b> The upper 24 bits of every int provided are IGNORED!
	 * The reason this overload exists is because Java refuses to perform bit level
	 * operations on any primitive smaller than an int.
	 * @param key AES key as an array of bytes 0-extended to ints.
	 */
	public AES(int[] key)
	{
		aes_key = new byte[16];
		for(int i = 0; i < key.length; i++) aes_key[i] = (byte)key[i];
	}
	
	/* ----- Static Tables ----- */
	
	/* ----- Decryption ----- */
	
	public byte[] decrypt(byte[] iv, byte[] in)
	{
		try 
		{
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			SecretKeySpec skey = new SecretKeySpec(aes_key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, skey, ivspec);
			return cipher.doFinal(in);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (NoSuchPaddingException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (InvalidAlgorithmParameterException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (IllegalBlockSizeException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (BadPaddingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}

	/* ----- Encryption ----- */
	
	public byte[] encrypt(byte[] iv, byte[] in)
	{
		try 
		{
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			SecretKeySpec skey = new SecretKeySpec(aes_key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, skey, ivspec);
			return cipher.doFinal(in);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (NoSuchPaddingException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (InvalidAlgorithmParameterException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (IllegalBlockSizeException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (BadPaddingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
