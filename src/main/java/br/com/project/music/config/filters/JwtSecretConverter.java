package br.com.project.music.config.filters;

import java.util.Base64;

public class JwtSecretConverter {

    public static void main(String[] args) {
        String hexSecret = "062a569fc4f8e8edb03a5323af481aab46eb0c887ce11427be1c122990493ece62fdd0e26855b057f5100719b55aa44d119e9dcf01e79baa8a1a10d4f1121767";
        byte[] decodedBytes = hexStringToByteArray(hexSecret);
        String base64Secret = Base64.getEncoder().encodeToString(decodedBytes);
        System.out.println("Chave Secreta Base64: " + base64Secret);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}