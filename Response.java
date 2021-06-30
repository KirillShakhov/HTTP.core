import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс, который хранит информацию из ответа сервера
 */
@Data
public class Response {
    /**
     * setCookie - cookie, которые отправил сервер (ключ, значение)
     */
    private Map<String, String> setCookies = new HashMap<>();
    /**
     * Хедеры, которые были в ответе сервера
     */
    Map<String, List<String>> headerFields = new HashMap<>();
    /**
     * Код ответа сервера
     */
    private int responseCode;
    /**
     * Информация, которую передал сервер. HTML или JSON
     */
    private String data = "";

    /**
     * Метод для добавления куки
     * @param key ключ
     * @param value значение
     */
    public void addSetCookie(String key, String value){
        setCookies.put(key, value);
    }

    @Override
    public String toString() {
        return "Response{" +
                "setCookies=" + setCookies +
                ", headerFields=" + headerFields +
                ", responseCode=" + responseCode +
                ", data='" + data + '\'' +
                '}';
    }
}
