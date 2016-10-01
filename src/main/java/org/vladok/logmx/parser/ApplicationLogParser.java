package org.vladok.logmx.parser;

import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;
import org.vladok.logmx.parser.udata.UDataFormatter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladislav Okulich-Kazarin
 *         Date: 24.09.2016
 *         Time: 20:11
 */
public class ApplicationLogParser extends LogFileParser {

    /** Current parsed log entry */
    private ParsedEntry entry = null;

    /** Entry date format */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");

    /** Mutex to avoid that multiple threads use the same Date formatter at the same time */
    private final Object DATE_FORMATTER_MUTEX = new Object();

    /** Pattern for entry begin */
    // 22-09-16 12:46:13.412 [main] INFO - Инициализация модулей...
    private final static Pattern ENTRY_BEGIN_PATTERN = Pattern
            .compile("^(?<timestamp>\\d{2}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) " +
                    "\\[(?<thred>[^]]+)\\] " +
                    "(?<level>[A-Z]+) - (?<message>.*)$");

    /** Buffer for Entry message (improves performance for multi-lines entries)  */
    private StringBuilder entryMsgBuffer = null;

    /** Key of user-defined field "formatted" */
    private static final String EXTRA_FORMATTED_FIELD_KEY = "Formatted";

    /** User-defined fields names (here, only one) */
    private static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList(EXTRA_FORMATTED_FIELD_KEY);

    protected void parseLine(String line) throws Exception {
        // If end of file, records last entry if necessary, and exits
        if (line == null) {
            recordPreviousEntryIfExists();
            return;
        }

        Matcher matcher = ENTRY_BEGIN_PATTERN.matcher(line);
        if (matcher.matches()) {
            // Record previous found entry if exists, then create a new one
            prepareNewEntry();
            entry.setDate(matcher.group("timestamp"));
            entry.setLevel(matcher.group("level"));
            entry.setThread(matcher.group("thred"));
            entryMsgBuffer.append(matcher.group("message"));
        } else if (entry != null) {
            entryMsgBuffer.append('\n').append(line); // appends this line to previous entry's text
        }
    }


    @Override
    public boolean isEmitterFieldProvided() {
        return false;
    }

    @Override
    public List<String> getUserDefinedFields() {
        return EXTRA_FIELDS_KEYS;
    }

    public Date getRelativeEntryDate(ParsedEntry parsedEntry) throws Exception {
        return getAbsoluteEntryDate(parsedEntry);
    }

    public Date getAbsoluteEntryDate(ParsedEntry parsedEntry) throws Exception {
        synchronized (DATE_FORMATTER_MUTEX) { // Java date formatter is not thread-safe
            return dateFormat.parse(parsedEntry.getDate());
        }
    }

    public String getParserName() {
        return "Rainbow Application.log Parser";
    }

    public String getSupportedFileType() {
        return "*.log";
    }

    /**
     * Пытаемся сформатировать UData, если UData не найдена, возвращаем null
     *
     * @param message сообщение
     * @return форматированное сообщение или null
     */
    private String formatUData(String message) {
        if (message == null)
            return null;
        // Не содержит UData
        if ( ! message.contains("[ TYPE: '")) {
            return null;
        }
        try {
            return UDataFormatter.format(message);
        } catch (Exception e) {
            return "<UDataFormatterException>";
        }
    }


    /**
     * Send to LogMX the current parsed log entry
     * @throws Exception
     */
    private void recordPreviousEntryIfExists() throws Exception {
        if (entry != null) {
            entry.setMessage(entryMsgBuffer.toString());
            String formattedUdata = formatUData(entry.getMessage());
            if (formattedUdata != null)
                entry.getUserDefinedFields().put(EXTRA_FORMATTED_FIELD_KEY, formattedUdata);
            addEntry(entry);
        }
    }

    /**
     * Send to LogMX the current parsed log entry, then create a new one
     * @throws Exception
     */
    private void prepareNewEntry() throws Exception {
        recordPreviousEntryIfExists();
        entry = createNewEntry();
        entryMsgBuffer = new StringBuilder(80);
        entry.setUserDefinedFields(new HashMap<String, Object>(1)); // Create an empty Map with only one element allocated
    }
}
