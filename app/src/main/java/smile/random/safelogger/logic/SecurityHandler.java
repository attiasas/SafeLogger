package smile.random.safelogger.logic;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Author : Assaf Attias
 * Handles All Security algorithms (Hash,Encrypt,Decrypt)
 */
public class SecurityHandler {

    /**
     * Generate new random bytes as str representation (for salt/iv)
     * @return byte array of random bytes
     */
    public static byte[] generateRandomBytes()
    {
        SecureRandom random = new SecureRandom();
        byte[] gen = new byte[C.BLOCK_SIZE];
        random.nextBytes(gen);
        return gen;
    }

    /**
     * digest a msg with a salt into a hash representation
     * @param msg - plain txt to digest
     * @param salt - salt to add into msg
     * @return hashed msg in byte array
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hash(String msg, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(C.HASH_ALGORITHM);
        md.update(salt); // update salt to digest with
        byte[] hashedPassword = md.digest(msg.getBytes(StandardCharsets.UTF_8));
        return hashedPassword;
    }

    /**
     * Generate a secret key from a given password and a salt using encryption
     * @param password - plain txt password to generate secret key from
     * @param salt - salt to add into the key
     * @return a secret key generated from a password
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey getKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance(C.ENC_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, C.STR_PARAM, C.KEY_LEN);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), C.KEY_ALGORITHM);
        return secret;
    }

    /**
     * Encrypt a plain txt with a secret key and initialization vector
     * @param plain - txt to encrypt
     * @param key - secret to encrypt with
     * @param sIv - initialization vector to encrypt with
     * @return cipher, encrypted txt
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] encrypt(String plain, SecretKey key, byte[] sIv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        IvParameterSpec iv = new IvParameterSpec(sIv); // init vector
        Cipher cipher = Cipher.getInstance(C.ENC_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(plain.getBytes());
        //Base64.getEncoder().encodeToString(cipherText);
        return cipherText;
    }

    /**
     * Decrypt a given cipher with a secret and initialization vector
     * @param cipherText - cipher to decrypt
     * @param key - secret to decrypt with
     * @param sIv - initialization vector to decrypt with
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(byte[] cipherText, SecretKey key, byte[] sIv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        IvParameterSpec iv = new IvParameterSpec(sIv); // init vector
        Cipher cipher = Cipher.getInstance(C.ENC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(cipherText);
        //byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
