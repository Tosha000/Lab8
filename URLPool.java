package lab_8;

import java.util.LinkedList;

/*  Класс с именем URLPool, который будет хранить список
    всех URL-адресов для поиска, а также относительный "уровень" каждого из
    этих URL-адресов (также известный как "глубина поиска") */

public class URLPool {
    int maxDepth; // Максимальная глубина поиска
    int count_waitThread; // Счётчик, который хранит в себе значение количества потоков ожидания нового URL-адреса

    /* Будем сохранять URL-адреса и их глубину поиска вместе, как
    экземпляры класса с именем URLDepthPair, как это было сделано в прошлой
    лабораторной работе */
    LinkedList<URLDepthPair> not_viewed_url = new LinkedList<>(); // Список для непросмотренных ссылок
    LinkedList<URLDepthPair> viewed_url = new LinkedList<>(); // Список для просмотреных ссылок

    public URLPool(int maxDepth) { // Параметризованный конструктор класса
        this.maxDepth = maxDepth;
        count_waitThread = 0; // Начальное значениё счётчика = 0
    }

    // Поточно-ориентированная операция получения пары URL-глубина из пула
    // synchronized - ключевое слово, которое позволяет заблокировать доступ к методу или части кода, если его уже использует другой поток
    public synchronized URLDepthPair getPair() {
        while (not_viewed_url.size() == 0) { // Если ни один адрес в настоящее время недоступен
            count_waitThread++; // Увеличивается непосредственно перед вызовом wait()
            try {
                wait(); // Метод освобождает монитор (монитор можно рассматривать как маленький ящик, одновременно хранящий только один поток исполнения)
                // и переводит вызывающий поток в состояние ожидания до тех пор, пока другой поток не вызовет метод notify()
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
            count_waitThread--; // Уменьшается сразу после выхода из режима ожидания
        }
        // Если адрес доступен
        // removeFirst() класса java.util.LinkedList в Java удаляет и возвращает первый элемент из списка непросмотренных ссылок
        // - это способ получения пары URL-глубина из пула и удаления этой пары из списка одновременно
        return not_viewed_url.removeFirst();
        // т.е. мы получаем первую пару, возвращаем её и удаляем из списка непросмотренных
    }

    // Поточно-ориентированная операция добавления пары URL-глубина к пулу непросмотренных URL-адресов
    public synchronized void addPair(URLDepthPair pair) {
        if (viewed_url.contains(pair) == false) { // Если просмотренные ссылки не содержат пару URL-глубины
            viewed_url.add(pair); // то добавить эту пару к пулу
            if (pair.get_depth() < maxDepth) { // если глубина пары URL-глубины меньше максимальной глубины поиска
                not_viewed_url.add(pair); // то добавить эту пару к непросмотренным ссылкам
                notify(); // Продолжает работу потока, у которого ранее был вызван метод wait()
            }
        }
    }

    public synchronized int getWait() {
        return count_waitThread;
    }

    public LinkedList<URLDepthPair> getChecked() {
        return viewed_url;
    }
}
