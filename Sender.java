import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.jetty.client.HttpProxy;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Класс отвечающий за отправку http запросов
 */
public class Sender implements ISender {
    /**
     * Поле, в котором будет храниться proxy
     */
    private Proxy webProxy = null;
    /**
     * Builder, который формируется заранее
     */
    private static HttpClientBuilder builder = null;
    static {
        try {
            builder = HttpClientBuilder.create().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    Sender(){ }

    Sender(Proxy webProxy){
        this.webProxy = webProxy;
    }

    /**
     * Метод для реализации HTTP запросов
     * @param request запрос, который будет отправляться
     * @return Response ответ сервера
     */
    public synchronized Response send(Request request) {
        try {
            if(request.getMethod().equals("PATCH")) {
                return sendPatch(request);
            }
            Response response = new Response();
            URL url = new URL(request.getLink());
            URLConnection con = webProxy != null ? url.openConnection(webProxy) : url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) con;
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod(request.getMethod());
            for(Map.Entry<String, String> entry : request.getRequestProperties().entrySet()){
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

            //DATA (JSON)
            if (!request.getData().isEmpty()) {
                byte[] out = request.getData().getBytes("UTF-8");
                int length = out.length;
                httpConnection.setFixedLengthStreamingMode(length);
                httpConnection.connect();
                try (OutputStream os = httpConnection.getOutputStream()) {
                    os.write(out);
                }
            } else {
                httpConnection.connect();
            }

            response.setResponseCode(httpConnection.getResponseCode());
            response.setHeaderFields(httpConnection.getHeaderFields());

            List<String> ls = httpConnection.getHeaderFields().get("Set-Cookie");
            if (ls != null) {
                for (String s : ls) {
                    String[] arr = s.split(";");
                    String[] token = arr[0].trim().split("=", 2);
                    response.addSetCookie(token[0], token[1]);
                }
            }

            if(request.isDoIn()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    String inputLine;
                    StringBuilder res = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        res.append(inputLine);
                    }
                    in.close();
                    response.setData(res.toString());
                } catch (Exception ignored){}
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Метод для реализации HTTP patch запроса
     * @param request запрос, который будет отправляться
     * @return Response ответ сервера
     */
    public synchronized Response sendPatch(Request request) {
        try {
            HttpClientBuilder builder = HttpClientBuilder.create().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

            if (webProxy != null) {
                HttpProxy proxy = new HttpProxy("", 123);
                //builder.setProxy();
            }

            CloseableHttpClient httpClient = builder.build();

            HttpPatch req = new HttpPatch(request.getLink());
            StringEntity params = new StringEntity(request.getData(), ContentType.APPLICATION_JSON);
            req.setEntity(params);

            for(Map.Entry<String, String> entry : request.getRequestProperties().entrySet()){
                req.addHeader(entry.getKey(), entry.getValue());
            }

            HttpResponse res = httpClient.execute(req);
            Response response = new Response();
            response.setResponseCode(res.getStatusLine().getStatusCode());
            response.setData(res.getEntity().toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
