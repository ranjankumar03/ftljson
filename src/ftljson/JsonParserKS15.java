package ftljson;

import java.nio.*;

import static ftljson.Unsafe.*;

public class JsonParserKS15 {

    private static final byte NUMERIC       = 0;
    private static final byte NUMERIC_START = 1;
    private static final byte BEGIN_OBJECT  = 2;
    private static final byte BEGIN_ARRAY   = 3;
    private static final byte FALSE         = 4;
    private static final byte TRUE          = 5;
    private static final byte NULL          = 6;
    private static final byte END_OBJECT    = 7;
    private static final byte END_ARRAY     = 8;
    private static final byte INVALID       = 9;
    private static final byte IGNORE        = 10;

    private static final ByteBuffer TYPES      = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
    private static final long       TYPES_ADDR = addressOf(TYPES);

    static {
        for (int i = 0; i < 256; i++) {
            TYPES.put(i, INVALID);
        }

        TYPES.put('\t', IGNORE);
        TYPES.put('\n', IGNORE);
        TYPES.put('\r', IGNORE);
        TYPES.put(' ', IGNORE);
        TYPES.put(':', IGNORE);
        TYPES.put(',', IGNORE);
        TYPES.put('+', NUMERIC);
        TYPES.put('-', NUMERIC_START);
        TYPES.put('.', NUMERIC);
        TYPES.put('e', NUMERIC);
        TYPES.put('E', NUMERIC);
        for (byte b = '0'; b <= '9'; b++) {
            TYPES.put(b, NUMERIC_START);
        }

        TYPES.put('{', BEGIN_OBJECT);
        TYPES.put('[', BEGIN_ARRAY);
        TYPES.put('f', FALSE);
        TYPES.put('t', TRUE);
        TYPES.put('n', NULL);
        TYPES.put('}', END_OBJECT);
        TYPES.put(']', END_ARRAY);
    }

    private JsonParserKS15() {
    }

    @SuppressWarnings("restriction")
	private static byte read(byte[] input, int pos) {
        return UNSAFE.getByte(null, TYPES_ADDR + (input[pos] & 0xFF));
    }

    private static boolean isEscaped(byte[] input, int pos) {
        boolean escaped = false;
        for (pos--; 0 <= pos; pos--) {
            if (input[pos] != '\\') {
                break;
            }
            escaped ^= true;
        }
        return escaped;
    }

    private static int skipIgnored(byte[] input, int pos) {
        for (pos++; pos < input.length; pos++) {
            if (read(input, pos) != IGNORE) {
                break;
            }
        }
        return pos;
    }

    private static int parseNumber(byte[] input, ParseListener listener, int pos) {
        int i;
        for (i = pos + 1; i < input.length; i++) {
            if (NUMERIC_START < read(input, i)) {
                break;
            }
        }
        listener.numberLiteral(pos, i-- - pos);
        return i;
    }

    private static int parseString(byte[] input, int pos) {
        for (pos++; pos < input.length && (input[pos] != '"' || isEscaped(input, pos)); pos++);
        return pos;
    }

    private static int json(byte[] input, ParseListener listener, boolean inobject, boolean cntn, int pos) {
        boolean recurseInObject;
        boolean recurseCntn;

        LOOP:
        do {
            pos = skipIgnored(input, pos);
            if (input[pos] == '"') {
                int start = pos + 1;
                pos = parseString(input, pos);
                if (inobject) {
                    listener.beginObjectEntry(start, pos - start);
                    recurseInObject = false;
                    recurseCntn = false;
                } else {
                    listener.stringLiteral(start, pos - start);
                    continue;
                }
            } else {
                switch (read(input, pos)) {
                    case NUMERIC_START:
                        pos = parseNumber(input, listener, pos);
                        continue;
                    case BEGIN_OBJECT:
                        listener.beginObject();
                        recurseInObject = true;
                        recurseCntn = true;
                        break;
                    case BEGIN_ARRAY:
                        listener.beginList();
                        recurseInObject = false;
                        recurseCntn = true;
                        break;
                    case FALSE:
                        listener.booleanLiteral(false);
                        pos += 4;
                        continue;
                    case TRUE:
                        listener.booleanLiteral(true);
                        pos += 3;
                        continue;
                    case NULL:
                        listener.nullLiteral();
                        pos += 3;
                        continue;
                    case END_OBJECT:
                        listener.endObject();
                        break LOOP;
                    case END_ARRAY:
                        listener.endList();
                        break LOOP;
                    default: // INVALID
                        throw new IllegalStateException();
                }
            }
            pos = json(input, listener, recurseInObject, recurseCntn, pos);
        } while (cntn);
        return pos;
    }

    public static int json(byte[] input, ParseListener listener) {
        return json(input, listener, false, false, -1);
    }

}