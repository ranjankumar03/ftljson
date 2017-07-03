package ftljson;

public class JsonParserKS6 {
	private int pos = -1;
	private static boolean ignore(byte b) {
		switch (b) {
		case '\t':
		case '\n':
		case '\r':
		case ' ':
		case ',':
		case ':':
			return true;
		}
		return false;
	}
	private static boolean isNumeric(byte c) {
		switch (c) {
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
		}
		return false;
	}
	private static boolean isNumberPart(byte c) {
		if (isNumeric(c)) return true;
		switch (c) {
		case '+':
		case '-':
		case '.':
		case 'e':
			return true;
		}
		return false;
	}
	private static byte read(byte[] input, int pos) {
		return input[pos];
	}
	private byte skipIgnored(byte[] input) {
		byte c;
		while (ignore(c = read(input, ++pos)));
		return c;
	}
	private boolean isEscaped(byte[] input) {
		int p = pos;
		boolean escaped = false;
		for (; read(input, --p) == '\\'; escaped ^= true);
		return escaped;
	}
	private void string(byte[] input, ParseListener listener, boolean inobject) {
		int start = pos + 1;
		while ((read(input, ++pos)) != '"' || isEscaped(input));
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
			switch (c) {
			case '"':
				string(input, listener, inobject);
				continue;
			}
			int p;
			if ((p = rest(input, listener, c)) != 0)
				return p;
		} while (cntn);
		return pos;
	}
	private int rest(byte[] input, ParseListener listener, byte c) {
		if (isNumeric(c) || c == '-') {
			number(input, listener);
			return 0;
		}
		switch (c) {
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
		case '{':
			listener.beginObject();
			json(input, listener, true, true);
			break;
		case '}':
			listener.endObject();
			return pos;
		}
		return 0;
	}
	public int json(byte[] input, ParseListener listener) {
		pos = -1;
		return json(input, listener, false, false);
	}
}