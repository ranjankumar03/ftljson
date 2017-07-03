package ftljson;

public class JsonParserKSS {

    private JsonParserKSS() {
    }

    private static boolean ignore(byte b) {
        if (b == ' ') {
            return true;
        }

        switch (b) {
            case '\t': // 9
            case '\n': // 10
            case '\r': // 13
                //case ' ': // 32
            case ',': // 44
            case ':': // 58
                return true;
        }

        return false;
    }

    private static boolean isNumberPart(byte c) {
        switch (c - '+') {
            case 0:  // '+' 43
            case 2:  // '-' 45
            case 3:  // '.' 46
            case 5:  // '0' 48
            case 6:  // '1' 49
            case 7:  // '2' 50
            case 8:  // '3' 51
            case 9:  // '4' 52
            case 10: // '5' 53
            case 11: // '6' 54
            case 12: // '7' 55
            case 13: // '8' 56
            case 14: // '9' 57
                return true;
        }

        return c == 'E' || c == 'e';
    }

    private static byte read(byte[] input, int pos) {
        return input[pos];
    }

    private static int skipIgnored(byte[] input, int pos) {
        // @formatter:off
        while (ignore(read(input, ++pos)));
        // @formatter:on
        return pos;
    }

    private static boolean isEscaped(byte[] input, int pos) {
        boolean escaped = false;
        while (read(input, --pos) == '\\') {
            escaped ^= true;
        }
        return escaped;
    }

    private static int string(byte[] input, ParseListener listener, boolean inobject, int pos) {
        int start = pos + 1;
        // @formatter:off
        while ((read(input, ++pos)) != '"' || isEscaped(input, pos));
        // @formatter:on
        if (inobject) {
            listener.beginObjectEntry(start, pos - start);
            pos = json(input, listener, false, false, pos);
        } else {
            listener.stringLiteral(start, pos - start);
        }
        return pos;
    }

    private static int number(byte[] input, ParseListener listener, int pos) {
        int start = pos;
        // @formatter:off
        while (isNumberPart(read(input, ++pos)));
        // @formatter:on
        listener.numberLiteral(start, pos - start);
        return pos - 1;
    }

    private static int json(byte[] input, ParseListener listener, boolean inobject, boolean cntn, int pos) {
        do {
            pos = skipIgnored(input, pos);
            byte c = read(input, pos);

            if (c == '"') { // 22
                pos = string(input, listener, inobject, pos);
                continue;
            }

            switch (c - '-') {
                case 0:  // '-' 45
                case 3:  // '0' 48
                case 4:  // '1' 49
                case 5:  // '2' 50
                case 6:  // '3' 51
                case 7:  // '4' 52
                case 8:  // '5' 53
                case 9:  // '6' 54
                case 10: // '7' 55
                case 11: // '8' 56
                case 12: // '9' 57
                    pos = number(input, listener, pos);
                    continue;
            }

            if (pos == (pos = rest(input, listener, pos, c))) {
                break;
            }
        } while (cntn);
        return pos;
    }

    private static int rest(byte[] input, ParseListener listener, int pos, byte c) {
        switch (c - '[') {
            case 0: // '[' 91
                listener.beginList();
                pos = json(input, listener, false, true, pos);
                break;
            case 2: // ']' 93
                listener.endList();
                break;
            case 11: // 'f' 102
                listener.booleanLiteral(false);
                pos += 4;
                break;
            case 19: // 'n' 110
                listener.nullLiteral();
                pos += 3;
                break;
            case 25: // 't' 116
                listener.booleanLiteral(true);
                pos += 3;
                break;
            case 32: // '{' 123
                listener.beginObject();
                pos = json(input, listener, true, true, pos);
                break;
            case 34: // '}' 125
                listener.endObject();
                break;
        }
        return pos;
    }

    public static int json(byte[] input, ParseListener listener) {
        return json(input, listener, false, false, -1);
    }
}