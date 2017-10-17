/** Класс-поток, предназначен для считывания команд
 * с консоли, которые вводит пользователь.
 * */

package ru.gnusinay.client;

import ru.gnusinay.protocol.Packet;
import ru.gnusinay.server.Config;
import ru.gnusinay.server.User;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


class ConsoleListener extends Thread {
    private User user;
    private Selector selector;
    private Queue<Packet> queue;
    private Semaphore semaphore;

    ConsoleListener(User user, Selector selector, Queue<Packet> queue, Semaphore semaphore) {
        this.user = user;
        this.selector = selector;
        this.queue = queue;
        this.semaphore = semaphore;
    }

    /** Метод в цикле ждет ввода команд с консоли, записывает очередную
     * команду в очередь, переключает канал в режиме запись и инициирует
     * событие на запись. В методе используется семафор, который позволяет
     * реализовать режим запрос-ответ с основным потоком клиентского
     * приложения
     * */
    public void run() {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        SelectionKey key = user.getChannel().keyFor(selector);
        while (true) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            String commandLine = scanner.nextLine();
                if (!isInterrupted() && user.isConnect()) {
                    CommandType commandType = CommandParser.match(commandLine);
                    if (commandType == CommandType.QUIT) {
                        try {
                            key.channel().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            System.exit(0);
                        }
                    }
                    if (commandType == CommandType.UNDEFINED) {
                        System.out.println(user.getCurDirectory() + "> " + Config.INCORRECT_COMMAND_CLIENT_MESSAGE);
                        System.out.print(user.getCurDirectory() + "> ");
                        semaphore.release();
                    } else {
                        String text = CommandParser.getParametersAsLine(commandType, commandLine, user.getCurDirectory());
                        Packet packet = new Packet(commandType, user.getName(), text);

                        queue.add(packet);
                        key.interestOps(SelectionKey.OP_WRITE);
                        selector.wakeup();
                    }
                } else {
                    break;
                }
        }
    }
}