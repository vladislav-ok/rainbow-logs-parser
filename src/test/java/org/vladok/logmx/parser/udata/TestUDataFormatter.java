package org.vladok.logmx.parser.udata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Тесты для форматера
 *
 * @author Vladislav Okulich-Kazarin
 *         Date: 01.10.2016
 *         Time: 18:39
 */
public class TestUDataFormatter {

    @Test
    public void noUDataString() throws Exception {
        String source = "Данные для клиента с id 105 успешно получены из БД. [fake udata]";
        String format = UDataFormatter.format(source);
        assertEquals(source, format);
    }

    @Test
    public void emptyData() throws Exception {
        String source = "[ TYPE: 'EMPTY_DATA'; DATA: '{}' ]";
        String expected = "[ TYPE: 'EMPTY_DATA'; DATA: '{}' ]";
        String format = UDataFormatter.format(source);
        assertEquals("Объект EMPTY_DATA не должен форматироваться", expected, format);
    }

    @Test
    public void emptyObject() throws Exception {
        String source = "[ TYPE: 'EMPTY_OBJECT'; DATA: '{}' ]";
        String expected =
                "[ TYPE: 'EMPTY_OBJECT'; DATA: '\n" +
                "    {}'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void testSimpleUObject() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{KEY=some value, KEY_2=val2, KEY3=}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        KEY=some value,\n" +
                "        KEY_2=val2,\n" +
                "        KEY3=\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void uObjectCommasInValue() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{KEY=some va, l,ue, KEY_2=,,,}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        KEY=some va, l,ue,\n" +
                "        KEY_2=,,,\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void uObjectOpenCloseBrackets() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{KEY={val} [], KEY_2={}}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        KEY={val} [],\n" +
                "        KEY_2={}\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void simpleArray() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{ARRAY=[1, some text2, 3]}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        ARRAY=[\n" +
                "            1,\n" +
                "            some text2,\n" +
                "            3\n" +
                "        ]\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void emptyArray() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{ARRAY=[]}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                        "    {\n" +
                        "        ARRAY=[]\n" +
                        "    }'\n" +
                        "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void arrayOpenCloseBrackets() throws Exception {
        String source = "[ TYPE: 'OBJECT'; DATA: '{ARRAY=[1, some[] }text2, 3}]}' ]";
        String expected =
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        ARRAY=[\n" +
                "            1,\n" +
                "            some[] }text2,\n" +
                "            3}\n" +
                "        ]\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void complexBusinessObject() throws Exception {
        String source = "[ TYPE: 'ABS_REQUEST'; DATA: '[ TYPE: 'IBANK_DOCUMENT'; DATA: '{PRINT=0, LOCAL_IP_ADDRESS=" +
                "0.0.0.0, CLIENT_NAME=ООО \"Пример\", OWNER_COMMENTS=, INN=7714698320, DOC_TYPE_ID=59, STATUS_TIME=" +
                "Thu Sep 22 12:46:24 MSK 2016, COMMENT=null, CLIENT_TYPE=0, DOC_TYPE=doc/person_info, STATUS=2, " +
                "SIGNER_LIST=[[ TYPE: 'IBANK_DOCUMENT_SIGN'; DATA: '{SIGN_TYPE=SIGN, SERIAL_TOKEN=, SIGN_DEPARTMENT_" +
                "CODE=0, OWNER_POSITION=представитель, OWNER_NAME=Иванов Иван Иванович, KEY_PROVIDER=std, TIMESTAMP=" +
                "22.09.2016 12:46:24, KEY_EXPIRATION_DATE=Sun Mar 29 00:00:00 MSK 2020, KEY_ID=14277244422914, SIGN_" +
                "LEVEL=0, ACTION_TYPE=1, KEY_CREATION_DATE=null}' ]], CLIENT_ID=105, EXT_CLIENT_ID=9562, CONTENT=[ T" +
                "YPE: 'IBANK_DOCUMENT_CONTENT'; DATA: '{TRUSTED_RECIPIENT=0, COMPILER_FIO=Вая Пупкин, PATRONYMIC=Раст" +
                "оргуевич, CLIENT_NAME=ООО \"Пример\", IDCARD_SERIES=6666, PROFIT_GETTER_FIO=Расторгуев Расторгуй Раст" +
                "оргуевич, INN=656565656565, BIRTHDATE=02.09.1991, AGREEMENT_TYPE=Агентский договор, LAST_NAME=Расторг" +
                "уев, FIRST_NAME=Расторгуй, IDCARD_ISSUER=УВД Расторгуево, DATE_DOC=22.09.2016, CITIZENSHIP=РОССИЯ, CL" +
                "IENT_ACCOUNT=40702978909044002699, AGREEMENT_END_DATE=11.09.2022, BIRTH_PLACE=село Расторгуево, PHONE" +
                "S=666-666-666, IS_BUSINESSMAN=0, CLIENT_BANK_NAME=ИНВЕСТСБЕРБАНК (ОАО), г.МОСКВА, IDCARD_ISSUE_DATE=10" +
                ".09.2015, PROFIT_GETTER_TYPE=Гражданин РФ, IDCARD_ISSUER_CODE=666-666, AGREEMENT_NUMBER=1, IDCARD_TYP" +
                "E=Паспорт гражданина РФ, ADDRESS=село Расторгуево, AGREEMENT_DATE=10.09.2015, IDCARD_NUMBER=666666}' ]" +
                ", BIC=044525311, CLIENT_COMMENT=null, DOC_ID=28672, GROUP_ID=null, GROUP_NAME=null, LAST_SIGN_DATE=Thu" +
                " Sep 22 12:46:24 MSK 2016, CONTRACT_NUMBER=, VERSION=1, REMOTE_IP_ADDRESS=0.0.0.0, BRANCH_ID=101, HAS" +
                "_ATTACHMENTS=false}' ], REQUEST_TYPE=SAVE, ATTACHMENTS=[]' ]";
        String expected =
                "[ TYPE: 'ABS_REQUEST'; DATA: '\n" +
                "    [ TYPE: 'IBANK_DOCUMENT'; DATA: '\n" +
                "        {\n" +
                "            PRINT=0,\n" +
                "            LOCAL_IP_ADDRESS=0.0.0.0,\n" +
                "            CLIENT_NAME=ООО \"Пример\",\n" +
                "            OWNER_COMMENTS=,\n" +
                "            INN=7714698320,\n" +
                "            DOC_TYPE_ID=59,\n" +
                "            STATUS_TIME=Thu Sep 22 12:46:24 MSK 2016,\n" +
                "            COMMENT=null,\n" +
                "            CLIENT_TYPE=0,\n" +
                "            DOC_TYPE=doc/person_info,\n" +
                "            STATUS=2,\n" +
                "            SIGNER_LIST=[\n" +
                "                [ TYPE: 'IBANK_DOCUMENT_SIGN'; DATA: '\n" +
                "                    {\n" +
                "                        SIGN_TYPE=SIGN,\n" +
                "                        SERIAL_TOKEN=,\n" +
                "                        SIGN_DEPARTMENT_CODE=0,\n" +
                "                        OWNER_POSITION=представитель,\n" +
                "                        OWNER_NAME=Иванов Иван Иванович,\n" +
                "                        KEY_PROVIDER=std,\n" +
                "                        TIMESTAMP=22.09.2016 12:46:24,\n" +
                "                        KEY_EXPIRATION_DATE=Sun Mar 29 00:00:00 MSK 2020,\n" +
                "                        KEY_ID=14277244422914,\n" +
                "                        SIGN_LEVEL=0,\n" +
                "                        ACTION_TYPE=1,\n" +
                "                        KEY_CREATION_DATE=null\n" +
                "                    }'\n" +
                "                ]\n" +
                "            ],\n" +
                "            CLIENT_ID=105,\n" +
                "            EXT_CLIENT_ID=9562,\n" +
                "            CONTENT=[ TYPE: 'IBANK_DOCUMENT_CONTENT'; DATA: '\n" +
                "                {\n" +
                "                    TRUSTED_RECIPIENT=0,\n" +
                "                    COMPILER_FIO=Вая Пупкин,\n" +
                "                    PATRONYMIC=Расторгуевич,\n" +
                "                    CLIENT_NAME=ООО \"Пример\",\n" +
                "                    IDCARD_SERIES=6666,\n" +
                "                    PROFIT_GETTER_FIO=Расторгуев Расторгуй Расторгуевич,\n" +
                "                    INN=656565656565,\n" +
                "                    BIRTHDATE=02.09.1991,\n" +
                "                    AGREEMENT_TYPE=Агентский договор,\n" +
                "                    LAST_NAME=Расторгуев,\n" +
                "                    FIRST_NAME=Расторгуй,\n" +
                "                    IDCARD_ISSUER=УВД Расторгуево,\n" +
                "                    DATE_DOC=22.09.2016,\n" +
                "                    CITIZENSHIP=РОССИЯ,\n" +
                "                    CLIENT_ACCOUNT=40702978909044002699,\n" +
                "                    AGREEMENT_END_DATE=11.09.2022,\n" +
                "                    BIRTH_PLACE=село Расторгуево,\n" +
                "                    PHONES=666-666-666,\n" +
                "                    IS_BUSINESSMAN=0,\n" +
                "                    CLIENT_BANK_NAME=ИНВЕСТСБЕРБАНК (ОАО), г.МОСКВА,\n" +
                "                    IDCARD_ISSUE_DATE=10.09.2015,\n" +
                "                    PROFIT_GETTER_TYPE=Гражданин РФ,\n" +
                "                    IDCARD_ISSUER_CODE=666-666,\n" +
                "                    AGREEMENT_NUMBER=1,\n" +
                "                    IDCARD_TYPE=Паспорт гражданина РФ,\n" +
                "                    ADDRESS=село Расторгуево,\n" +
                "                    AGREEMENT_DATE=10.09.2015,\n" +
                "                    IDCARD_NUMBER=666666\n" +
                "                }'\n" +
                "            ],\n" +
                "            BIC=044525311,\n" +
                "            CLIENT_COMMENT=null,\n" +
                "            DOC_ID=28672,\n" +
                "            GROUP_ID=null,\n" +
                "            GROUP_NAME=null,\n" +
                "            LAST_SIGN_DATE=Thu Sep 22 12:46:24 MSK 2016,\n" +
                "            CONTRACT_NUMBER=,\n" +
                "            VERSION=1,\n" +
                "            REMOTE_IP_ADDRESS=0.0.0.0,\n" +
                "            BRANCH_ID=101,\n" +
                "            HAS_ATTACHMENTS=false\n" +
                "        }'\n" +
                "    ],\n" +
                "    REQUEST_TYPE=SAVE,\n" +
                "    ATTACHMENTS=[]'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void textWithUData() throws Exception {
        String source = "Вызов сервиса: 'Сервис сохранения данных в системе iBank 2'. Данные запроса: " +
                "[ TYPE: 'IBANK_DOC_STATUS'; DATA: '{DOC_ID=28672, DOC_TYPE=person_info, " +
                "DESCRIPTION=Документ успешно исполнен, STATE=5, UNLOAD_RESULT=PROCESSED_SUCCEEDED, DOC_DATE=22.09.16}' ]";
        String expected =
                "Вызов сервиса: 'Сервис сохранения данных в системе iBank 2'. Данные запроса: \n" +
                "[ TYPE: 'IBANK_DOC_STATUS'; DATA: '\n" +
                "    {\n" +
                "        DOC_ID=28672,\n" +
                "        DOC_TYPE=person_info,\n" +
                "        DESCRIPTION=Документ успешно исполнен,\n" +
                "        STATE=5,\n" +
                "        UNLOAD_RESULT=PROCESSED_SUCCEEDED,\n" +
                "        DOC_DATE=22.09.16\n" +
                "    }'\n" +
                "]";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

    @Test
    public void textWithMultipleUData() throws Exception {
        String source = "Вызов сервиса: 'Сервис сохранения данных в системе iBank 2'. Данные запроса: " +
                "[ TYPE: 'IBANK_DOC_STATUS'; DATA: '{DOC_ID=28672, DOC_TYPE=person_info, " +
                "DESCRIPTION=Документ успешно исполнен, STATE=5, UNLOAD_RESULT=PROCESSED_SUCCEEDED, DOC_DATE=22.09.16}' ]" +
                " some text[ TYPE: 'OBJECT'; DATA: '{ARRAY=[]}' ] ending";
        String expected =
                "Вызов сервиса: 'Сервис сохранения данных в системе iBank 2'. Данные запроса: \n" +
                "[ TYPE: 'IBANK_DOC_STATUS'; DATA: '\n" +
                "    {\n" +
                "        DOC_ID=28672,\n" +
                "        DOC_TYPE=person_info,\n" +
                "        DESCRIPTION=Документ успешно исполнен,\n" +
                "        STATE=5,\n" +
                "        UNLOAD_RESULT=PROCESSED_SUCCEEDED,\n" +
                "        DOC_DATE=22.09.16\n" +
                "    }'\n" +
                "]\n" +
                "some text\n" +
                "[ TYPE: 'OBJECT'; DATA: '\n" +
                "    {\n" +
                "        ARRAY=[]\n" +
                "    }'\n" +
                "]\n" +
                "ending";
        String format = UDataFormatter.format(source);
        assertEquals(expected, format);
    }

}
