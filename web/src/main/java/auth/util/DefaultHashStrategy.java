package auth.util;

import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.auth.jdbc.JDBCHashStrategy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liulaoye on 17-6-6.
 * hash策略
 */
public class DefaultHashStrategy implements JDBCHashStrategy{

    private final PRNG random;

    public DefaultHashStrategy( Vertx vertx ) {
        random = new PRNG(vertx);
    }

    @Override
    public String generateSalt() {
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        return bytesToHex(salt);
    }

    @Override
    public String computeHash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            String concat = (salt == null ? "" : salt) + password;
            byte[] bHash = md.digest(concat.getBytes( StandardCharsets.UTF_8));
            return bytesToHex(bHash);
        } catch (NoSuchAlgorithmException e) {
            throw new VertxException(e);
        }
    }

    @Override
    public String getHashedStoredPwd(JsonArray row) {
        return row.getString(0);
    }

    @Override
    public String getSalt(JsonArray row) {
        return row.getString(1);
    }

    private final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int x = 0xFF & bytes[i];
            chars[i * 2] = HEX_CHARS[x >>> 4];
            chars[1 + i * 2] = HEX_CHARS[0x0F & x];
        }
        return new String(chars);
    }

    public static void main( String[] args ){
        byte[] a = new byte[2];
        a[0] = 1;
        a[1] = 10;
        System.out.println( new DefaultHashStrategy( Vertx.vertx() ).bytesToHex( a ));
    }
}