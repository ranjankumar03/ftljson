package ftljson;

public class JsonParserKS {
    private int pos = -1;
    private static boolean ignore(byte b) {
        switch (b) {
        case '\t': // 9
        case '\n': // 10
        case '\r': // 13
        case ' ':  // 32
        case ',':  // 44
        case ':':  // 58
            return true;
        }
        return false;
    }
    private static boolean isNumberPart(byte c) {
        switch (c) {
        case '+':
        case '-':
        case '.':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return true;
        case 'E':
        case 'e':
            return true;
        }
        return false;
    }
    private byte read(byte[] input, int pos) {
        return input[pos];
    }
    private byte skipIgnored(byte[] input) {
        byte c;
        while (ignore(c = read(input, ++pos)));
        return c;
    }
    private boolean isescaped(byte[] input) {
        int p = pos - 1;
        byte c = 0;
        while (read(input, p--) == '\\')
            c++;
        return (c & 1) == 1;
    }
    private void string(byte[] input, ParseListener listener, boolean inobject) {
        int start = pos + 1;
        while ((read(input, ++pos)) != '"' || isescaped(input));
        if (inobject) {
            listener.beginObjectEntry(start, pos - start);
            json(input, listener, false, false);
        } else {
            listener.stringLiteral(start, pos - start);
        }
    }

    private void number(byte[] input, ParseListener listener) {
        int start = pos;
        while (isNumberPart(read(input, ++pos)));
        listener.numberLiteral(start, pos-- - start);
    }

    private int json(byte[] input, ParseListener listener, boolean inobject, boolean cntn) {
        do {
            byte c = skipIgnored(input);
            switch (c) { // <- will become tableswitch
            case '"':
                string(input, listener, inobject);
                continue;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                number(input, listener);
                break;
            case '{':
                listener.beginObject();
                json(input, listener, true, true);
                break;
            case '}':
                listener.endObject();
                return pos;
            case '[':
                listener.beginList();
                json(input, listener, false, true);
                break;
            case ']':
                listener.endList();
                return pos;
            case 'f':
                listener.booleanLiteral(false);
                pos += 4;
                break;
            case 'n':
                listener.nullLiteral();
                pos += 3;
                break;
            case 't':
                listener.booleanLiteral(true);
                pos += 3;
                break;
            }
        } while (cntn);
        return pos;
    }
    public int json(byte[] input, ParseListener listener) {
        pos = -1; // <- to reuse same parser
        return json(input, listener, false, false);
    }
}