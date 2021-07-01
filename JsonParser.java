import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class JsonParser {

    public static String getValue(String fullStr, String subStr){
        boolean flag = false;
        int count = 0;
        byte[] fullStrBytes = new byte[0];
        try {
            fullStrBytes = fullStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = fullStr.indexOf(subStr) + subStr.length(); i < fullStrBytes.length; i++) {
            if (fullStrBytes[i] == '"') {
                count++;
                flag = true;
            }
            if (flag){
                i++;
                flag = false;
            }
            if (count == 2){
                stringBuilder.append((char) fullStrBytes[i]);
            }
            if (count == 3) break;
        }
        return stringBuilder.toString();
    }

    //","availability":3,"
    public static int getIntValue(String fullStr, String subStr){
        boolean flag = false;
        byte[] fullStrBytes = new byte[0];
        try {
            fullStrBytes = fullStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = fullStr.indexOf(subStr) + subStr.length(); i < fullStrBytes.length; i++){
            if (fullStrBytes[i] == ','){
                return Integer.parseInt((stringBuilder.toString()));
            }
            if (flag){
                stringBuilder.append((char) fullStrBytes[i]);
            }
            if (fullStrBytes[i] == ':') {
                flag = true;
            }
        }
        return 0;
    }
}
