package org.vladok.logmx.parser.udata;

import java.util.Stack;

import static org.vladok.logmx.parser.udata.UDataFormatter.Token.*;

/**
 * Класс предназначен для форматирования строки содержащей один или несколько элементов в формате UData
 *
 * @author Vladislav Okulich-Kazarin
 *         Date: 24.09.2016
 *         Time: 22:31
 */
public class UDataFormatter {

    /** Стэк для хранения текущей вложенности */
    private Stack<Token> stack = new Stack<Token>();

    private static final char NEW_LINE = '\n';
    private static final String INDENT = "    ";
    private static final String EMPTY_DATA = "[ TYPE: 'EMPTY_DATA'; DATA: '{}' ]";
    private static final String BUSINESS_OBJ_BEGINNING = "[ TYPE: '";
    private static final String BUSINESS_OBJ_END = "' ]";
    /** Исходная строка в виде массива для удобства обращения к элементам */
    private final char[] source;
    /** Исходная строка */
    private final String sourceStr;
    /** Текущая позиция в исходной строке */
    private int pos = 0;
    /** Результирующая строка */
    private StringBuilder result;


    private UDataFormatter(String source) {
        this.sourceStr = source;
        this.source = source.toCharArray();
        result = new StringBuilder(this.source.length * 2);
    }

    /**
     * Форматирует произвольную строку содержащую бизнес объекты
     *
     * @param source исходная строка
     * @return строка с отформатированными бизнес-объектами
     */
    public static String format(String source) throws UDataFormatterException {
        try {
            return new UDataFormatter(source).format();
        } catch (Exception e) {
            throw new UDataFormatterException("Ошибка при форматировании строки", e);
        }

    }

    /**
     * Форматирует произвольную строку содержащую бизнес объекты
     *
     * @return строка с отформатированными бизнес-объектами
     */
    private String format() {
        while ((pos + 2) < source.length) {
            if (isBusinessObjStart()) {
                if (pos > 0) // Перенос не нужен если мы в самом начале
                    result.append(NEW_LINE);
                processBusinessObject();
                if (pos < source.length) // Перенос не нужен если мы в самом конце
                    result.append(NEW_LINE);
            } else {
                result.append(source[pos]);
            }
            ++pos;
        }
        while (pos < source.length) {
            result.append(source[pos++]);
        }
        return result.toString();
    }

    /**
     * @return находмися ли мы в начале бизнес-объекта
     */
    private boolean isBusinessObjStart() {
        // Если после скобки идет пробел и "Т", то это бизнес объект
        // например [ TYPE: 'IBANK_DOCUMENT'; DATA: '
        return matchesNext(BUSINESS_OBJ_BEGINNING);
    }

    /**
     * Обработка бизнес-объекта
     */
    private void processBusinessObject() {
        if (matchesNext(EMPTY_DATA)) {
            result.append(EMPTY_DATA);
            pos += EMPTY_DATA.length();
            return;
        }
        stack.push(COMMON_BUSINESS_OBJECT);
        int singleQuoteCount = 0;
        while (singleQuoteCount < 3) {
            result.append(source[pos]);
            // Считаем одинарные ковычки, их должно быть 3
            // [ TYPE: 'IBANK_DOCUMENT'; DATA: '
            if (source[pos++] == '\'')
                singleQuoteCount++;
        }
        while ( ! isBusinessObjEnd()) {
            result.append(NEW_LINE);
            processBusinessObjectEntry();
        }
        stack.pop();
        // '
        result.append(source[pos++]);
        // пропускаем пробел
        ++pos;
        result.append(NEW_LINE);
        indent();
        // ]
        result.append(source[pos++]);
    }

    private boolean isBusinessObjEnd() {
        return matchesNext(BUSINESS_OBJ_END);
    }

    /**
     * Обработка элемента данных бизнес-объекта.
     * Может являться UDataObject или списком бизнес-объектов и пар ключ-значение
     */
    private void processBusinessObjectEntry() {
        indent();
        if (source[pos] == '{') {
            processUObject();
        } else if (source[pos] == '[') {
            processBracketOpen();
        } else {
            processKeyValue(BUSINESS_OBJ_END);
        }
        if (source[pos] == ',') {
            result.append(source[pos++]);
            // Пропускаем пробел после запятой
            ++pos;
        }
    }

    /**
     * Обработка простого UDataObject имеющего вид: {KEY=VALUE, KEY2=VALUE2, ...}
     */
    private void processUObject() {
        // Пустой объект {}
        if (source[pos + 1] == '}') {
            result.append(source[pos++]).append(source[pos++]);
            return;
        }
        stack.push(UOBJECT);
        // {
        result.append(source[pos++]).append(NEW_LINE);
        while (source[pos] != '}') {
            indent();
            processKeyValue("}");
            result.append(NEW_LINE);
        }
        stack.pop();
        indent();
        result.append(source[pos++]);
    }

    /**
     * Обработка пары ключ-занчение
     * KEY=VALUE
     * @param expectedEnd ожидаемое окончание последовательности ключ-значение
     */
    private void processKeyValue(String expectedEnd) {
        processUObjectFieldName();
        if (source[pos] == '[') {
            processBracketOpen();
        } else {
            // иначе просто константа
            processConst(expectedEnd);
        }
        while (source[pos] != ',' && ! matchesNext(expectedEnd)) {
            result.append(source[pos]);
            ++pos;
        }
        if (source[pos] == ',') {
            result.append(source[pos++]);
            // Пропускаем пробел после запятой
            ++pos;
        }
    }

    /**
     * Имя поля из пары ключ-значение
     * SOME_FIELD=
     */
    private void processUObjectFieldName() {
        while (source[pos] != '=') {
            result.append(source[pos]);
            ++pos;
        }
        // =
        result.append(source[pos++]);
    }

    /**
     * Обрабтка константного значения из пары ключ-значение
     *
     * @param expectedEnd ожидаемое окончание последовательности ключ-значение
     */
    private void processConst(String expectedEnd) {
        // Счетчик символов противоположных ожидаемому концу
        // т.е. если ожидаемый конец это },
        // то мы увеличиваем счетчик при встрече { и уменьшаем при }
        int openStack = 0;
        while ( ! lookAheadUObjectEntry()) {
            if (isOpposite(source[pos], expectedEnd)) {
                ++openStack;
            }
            // Конец выходим
            if (matchesNext(expectedEnd)) {
                --openStack;
                if (openStack < 0)
                    return;
            }
            result.append(source[pos]);
            ++pos;
        }
    }

    /**
     * Проверяет является ли символ началом для указаного конца.
     * Т.е. возвращает true если ch является открывающей скобкой, а end соответсвующей закрывающей
     *
     * @param ch проверяемый символ
     * @param end закрывающая последовательность
     * @return true если ch является открывающей скобкой, а end соответсвующей закрывающей
     */
    private boolean isOpposite(char ch, String end) {
        if ("}".equals(end)) {
           return ch == '{';
        }
        if ("]".equals(end)) {
            return ch == '[';
        }
        return false;
    }

    /**
     * Смотрим вперед в ожидании корректного начала элемента UObject т.е. ожидаем ", SOME_KEY="
     *
     * @return true, если дльше корретный элемент
     */
    private boolean lookAheadUObjectEntry() {
        int i = pos;
        if (source[i] != ',' || source[i + 1] != ' ')
            return false;
        ++i;
        while (source[++i] != '=') {
            // Ключ длжен состоять из заглавных букв цифр или подчеркивания
            if ( ! ((source[i] >= 'A' && source[i] <= 'Z') || (source[i] >= '0' && source[i] <= '9') || source[i] == '_')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Обрабатываем возможные варианты начинающиеся с [
     * либо бизнес-объект либо массив
     */
    private void processBracketOpen() {
        if (matchesNext(BUSINESS_OBJ_BEGINNING)) {
            processBusinessObject();
        } else {
            processArray();
        }
    }

    /**
     * Обраоботка массива, например: [add, gg[], 123 foo]
     */
    private void processArray() {
        // Пустой массив []
        if (source[pos + 1] == ']') {
            result.append(source[pos++]).append(source[pos++]);
            return;
        }
        stack.push(ARRAY);
        // [
        result.append(source[pos++]).append(NEW_LINE);
        while (source[pos] != ']') {
            processArrayElement();
        }
        stack.pop();
        indent();
        // ]
        result.append(source[pos++]);
    }

    /**
     * Обработка элемента массива (списка)
     */
    private void processArrayElement() {
        indent();
        if (source[pos] == '[') {
            processBracketOpen();
        }
        // Счетчик символов противоположных ожидаемому концу
        // т.е. если ожидаемый конец это ],
        // то мы увеличиваем счетчик при встрече [ и уменьшаем при ]
        int openStack = 0;
        // элементы разделяются запятой и пробелом
        while (!matchesNext(", ")) {
            if (source[pos] == '[') {
                ++openStack;
                // Конец массива - выходим
            } else if (source[pos] == ']') {
                --openStack;
                if (openStack < 0) {
                    result.append(NEW_LINE);
                    return;
                }
            }
            result.append(source[pos]);
            ++pos;
        }
        // ,
        result.append(source[pos++]).append(NEW_LINE);
        // Пропускаем пробел
        ++pos;
    }


    /**
     * Добавить отступ на основе колличества элементов в стеке (уровень вложенности)
     */
    private void indent() {
        for (int i = 0; i < stack.size(); i++) {
            result.append(INDENT);
        }
    }

    /**
     * Сравнивает иходную строку с указанной начиная с текущей позиции.
     * Те мы проверяем что подстрока начиная с pos и длиной str.length() равна str
     *
     * @param str строка для сравнения
     * @return true если строка строка str содержится после текущей позиции
     */
    private boolean matchesNext(String str) {
        return sourceStr.regionMatches(pos, str, 0, str.length());
    }

    enum Token {
        /** Массив элементов: [a, b, c] */
        ARRAY,
        /** {KEY=value, ...} */
        UOBJECT,
        /** [ TYPE: 'IBANK_DOCUMENT'; DATA: '...'] */
        COMMON_BUSINESS_OBJECT
    }

}
