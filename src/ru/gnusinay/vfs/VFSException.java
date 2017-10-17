package ru.gnusinay.vfs;

/** Исключение возникающее при работе с файловой системой
 */
public class VFSException extends Exception {

    public VFSException(String message) {
        super(message);
    }
}
