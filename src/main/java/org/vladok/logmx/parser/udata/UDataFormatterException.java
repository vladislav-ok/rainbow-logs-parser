package org.vladok.logmx.parser.udata;

/**
 * Исключения возникающие во время форматирования
 *
 * @author Vladislav Okulich-Kazarin
 *         Date: 01.10.2016
 *         Time: 19:11
 */
public class UDataFormatterException extends Exception {

    public UDataFormatterException(String message) {
        super(message);
    }

    public UDataFormatterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UDataFormatterException(Throwable cause) {
        super(cause);
    }
}
