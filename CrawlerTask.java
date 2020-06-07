package lab_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

// Чтобы выполнялось веб-сканирование в нескольких потоках, необходим
// класс CrawlerTask, который реализует интерфейс Runnable (интерфейс предоставляет абстракцию единицы исполняемого кода)
public class CrawlerTask implements Runnable {
    public static void main(String[] args) {
        // Ввода пользователем значений, а именно: адреса сайта и желаемой глубины поиска
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите адрес сайта: ");
        String host1 = scanner.nextLine(); // Необходимо ввести адрес веб-сайта
        String host = "https://" + host1 + "/";
        System.out.println("Введите максимальную глубину поиска: ");
        int depthMax = scanner.nextInt(); // а также ввести максимальную глубину поиска
        System.out.println("Введите количество потоков: ");
        int countThread = scanner.nextInt();

        URLPool pool = new URLPool(depthMax);
        pool.addPair(new URLDepthPair(host, 0));

        for (int m = 0; m < countThread; m++) {
            // Каждой задаче поискового робота нужно дать ссылку на созданный пул URL-адресов
            Thread t = new Thread(new CrawlerTask(pool)); // Пул URL-адресов - это список, содержащий в себе URL-адреса
            t.start(); // Запуск потока исполнения
        }
        // Если общее количество потоков равно количеству потоков, которое вернул соответствующий метод, необходимо вызвать System.exit() для завершения работы
        while (pool.getWait() != countThread) {
        }
        int count = 0; // Счётчик кол-ва просмотренных ссылок
        for (URLDepthPair pair : pool.getChecked()) {
            count++;
            // "%s%-90s%s%n" - форматирование выводимой строки
            System.out.printf("%s%-90s%s%n", count + " " + "просмотренная ссылка:", pair.get_url(), "глубина поиска: " + pair.get_depth());
        }
        System.exit(0);
    }

    // Каждыq экземпляр CrawlerTask должен иметь ссылку на один экземпляр класса URLPool
    URLPool pool;

    public CrawlerTask(URLPool pool) { // Конструктор класса
        this.pool = pool;
    }

    public void searchURL() throws IOException {
        // Socket (String host, int port) создает новый сокет из полученной строки с
        // именем хоста и из целого числа с номером порта, и устанавливает соединение
        Socket socket;
        URLDepthPair pair = pool.getPair(); // Получим пару URL-глубина из пула
        int depth = pair.get_depth();
        try {
            socket = new Socket(pair.get_host(), 80); // Для HTTP соединений обычно используется порт 80
        } catch (UnknownHostException e) {
            System.err.println(e);
            return;
        }
        socket.setSoTimeout(2000); // Время ожидания нового сокета


        PrintWriter myWriter = new PrintWriter(socket.getOutputStream(), true);
        myWriter.println("GET " + pair.get_path() + " HTTP/1.1");
        myWriter.println("Host: " + pair.get_host());
        myWriter.println("Connection: close");
        myWriter.println();

        // Тут реализовано чтение и проверка ссылки
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String str;
        while ((str = reader.readLine()) != null) { // Пока есть ссылка
            while (str.contains("href=\"")) { // которая содержит атрибут href (путь к файлу, на который дается ссылка (URL))
                String content;
                try {
                    str = str.substring(str.indexOf("href=\"") + 6);
                    content = str.substring(0, str.indexOf('\"'));
                    if (!content.startsWith("http")) // Если ссылка после атрибута href не начинается с "http"
                        content = content.startsWith("/") ? pair.get_link() + content : pair.get_link() + "/" + content;
                } catch (StringIndexOutOfBoundsException e) {
                    break;
                }
                pool.addPair(new URLDepthPair(content, depth + 1)); // Добавим пару URL-глубина к пулу
            }
        }
        reader.close(); // Закрытие чтения
        socket.close(); // Закрытие сокета
    }

    @Override
    public void run() { // Точка входа в поток исполнения
        while (true) {
            try {
                searchURL();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}