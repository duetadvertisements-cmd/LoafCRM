package javaServers.models;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class passwordHasher {
    public String hashPassword (String password) throws Exception{
        MessageDigest md = MessageDigest.getInstance ("SHA-256");
        byte[] hashBytes = md.digest (password.getBytes (StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder ();
        for (byte b : hashBytes) {
            hex.append (String.format ("%02x", b));
        }
        String hashedPassword = new String (hex);

        return hashedPassword;
    }

    public boolean verifyPassword(String password, String storedHashedPassword) throws Exception{
        return hashPassword(password).equals(storedHashedPassword);
    }
}
