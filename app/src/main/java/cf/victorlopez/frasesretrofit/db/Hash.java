package cf.victorlopez.frasesretrofit.db;

public class Hash {
    /**
     * Método que devuelve un Hash a partir de un texto
     * @param txt Texto que convertir
     * @return Hash SHA1
     */
    public static String getHash(String txt) {
        String hashType = "SHA1";
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
