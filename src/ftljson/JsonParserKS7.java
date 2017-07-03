package ftljson;

public class JsonParserKS7 {
    private int pos = -1;
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
    private byte read(byte[] input, int pos) {
        return input[pos];
    }
    private byte skipIgnored(byte[] input) {
        byte c;
        // @formatter:off
        while (ignore(c = read(input, ++pos)));
        // @formatter:on
        return c;
    }
    private boolean isEscaped(byte[] input) {
        int p = pos - 1;
        boolean escaped = false;
        while (read(input, p--) == '\\') {
            escaped ^= true;
        }
        return escaped;
    }
    private void string(byte[] input, ParseListener listener, boolean inobject) {
        int start = pos + 1;
        // @formatter:off
        while ((read(input, ++pos)) != '"' || isEscaped(input));
        // @formatter:on
        if (inobject) {
            listener.beginObjectEntry(start, pos - start);
            json(input, listener, false, false);
        } else {
            listener.stringLiteral(start, pos - start);
        }
    }
    private void number(byte[] input, ParseListener listener) {
        int start = pos;
        // @formatter:off
        while (isNumberPart(read(input, ++pos)));
        // @formatter:on
        listener.numberLiteral(start, pos-- - start);
    }
    private int json(byte[] input, ParseListener listener, boolean inobject, boolean cntn) {
        do {
            byte c = skipIgnored(input);

            if (c == '"') { // 22
                string(input, listener, inobject);
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
                    number(input, listener);
                    continue;
            }

            switch (c - '[') {
                case 0: // '[' 91
                    listener.beginList();
                    json(input, listener, false, true);
                    continue;
                case 2: // ']' 93
                    listener.endList();
                    return pos;
                case 11: // 'f' 102
                    listener.booleanLiteral(false);
                    pos += 4;
                    continue;
                case 19: // 'n' 110
                    listener.nullLiteral();
                    pos += 3;
                    continue;
                case 25: // 't' 116
                    listener.booleanLiteral(true);
                    pos += 3;
                    continue;
                case 32: // '{' 123
                    listener.beginObject();
                    json(input, listener, true, true);
                    continue;
                case 34: // '}' 125
                    listener.endObject();
                    return pos;
            }
        } while (cntn);
        return pos;
    }
    public int json(byte[] input, ParseListener listener) {
        pos = -1;
        return json(input, listener, false, false);
    }
}