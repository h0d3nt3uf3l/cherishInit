package com.niceshit.cherishinit;

import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KodakSecret extends AppCompatActivity {

    public static SecretKey generateSecretKey(int i2) {
        try {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(i2, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e2) {
            System.out.println("AES secret key spec error");
            return null;
        }
    }

    private static String bytesToHex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        if (bArr != null) {

            for (byte b : bArr) {
                sb.append(String.format(Locale.US, "%02x", Byte.valueOf((byte) (b & 255))));
            }
        }
        return sb.toString();
    }

    public static String rsaEncryptSecretKeyToHex(Key key, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(1, key);
            return bytesToHex(cipher.doFinal(secretKey.getEncoded()));
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String getSecretIv(String str) {
        StringBuilder sb = new StringBuilder();
        if (str != null) {
            int length = str.length();
            if (length <= 16) {
                sb.append(str);
                for (int i2 = 0; i2 < 16 - length; i2++) {
                    sb.append((char) 0);
                }
            } else {
                sb.append((CharSequence) str, 0, 16);
            }
        } else {
            for (int i3 = 0; i3 < 16; i3++) {
                sb.append((char) 0);
            }
        }
        return sb.toString();
    }

    public static byte[] aesZeroPadding(byte[] bArr) {
        int i2;
        if (bArr != null) {
            int length = bArr.length;
            if (length % 16 == 0) {
                i2 = length;
            } else {
                i2 = ((length / 16) + 1) * 16;
            }
            byte[] bArr2 = new byte[i2];
            if (length > 0) {
                Arrays.fill(bArr2, (byte) 0);
                System.arraycopy(bArr, 0, bArr2, 0, length);
                return bArr2;
            }
            return bArr2;
        }
        return null;
    }
    public static String aesEncryptToHexWithPadding(SecretKey secretKey, String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, secretKey, new IvParameterSpec(getSecretIv("cinatic_2018").getBytes()));
            return bytesToHex(cipher.doFinal(aesZeroPadding(str.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }
    public static PublicKey getPublicKeyFromString(String str) throws IOException, GeneralSecurityException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(str.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", ""), 0)));
    }
}
