package lab_8;

import java.net.MalformedURLException;
import java.net.URL;

public class URLDepthPair {

    private String host;
    private int depth;
    private URL url;

    public URLDepthPair(String host, int depth) {
        this.host = host;
        this.depth = depth;
        try {
            url = new URL(host);
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return;
        }
    }

    // Получение URL-адреса
    public String get_url() {
        return host;
    }

    // Получение глубины
    public int get_depth() {
        return depth;
    }

    // Получение хоста
    public String get_host() {
        return url.getHost();
    }

    // Получение пути
    public String get_path() {
        return url.getPath();
    }

    // Получение ссылки
    public String get_link() {
        return url.getProtocol() + "://" + url.getHost();
    }
}