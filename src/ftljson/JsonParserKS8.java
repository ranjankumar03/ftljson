package ftljson;

public class JsonParserKS8 {
	private static final boolean[] ignoreCases = new boolean[256];
	private static final boolean[] numericCases = new boolean[256];
	static {
		ignoreCases['\t'] = true;
		ignoreCases['\n'] = true;
		ignoreCases['\r'] = true;
		ignoreCases[' '] = true;
		ignoreCases[','] = true;
		ignoreCases[':'] = true;
		numericCases['+'] = true;
		numericCases['-'] = true;
		numericCases['.'] = true;
		numericCases['e'] = true;
		numericCases['E'] = true;
		for (byte b = '0'; b <= '9'; b++)
			numericCases[b] = true;
	}
	private int pos = -1;
	private static boolean ignore(byte b) {
		return ignoreCases[b];
	}
	private static boolean isNumberPart(byte c) {
		return numericCases[c];
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
			case '{':
				listener.beginObject();
				json(input, listener, true, true);
				continue;
			}
			int p;
			if ((p = rest(input, listener, c)) != 0)
				return p;
		} while (cntn);
		return pos;
	}
	private int rest(byte[] input, ParseListener listener, byte c) {
		if (isNumberPart(c)) {
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