package ftljson;

import static ftljson.Unsafe.UNSAFE;
import static ftljson.Unsafe.addressOf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JsonParserKS9 {
	private static final ByteBuffer IGNORE = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
	private static final long IGNORE_ADDR = addressOf(IGNORE);
	private static final ByteBuffer NUMERIC = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
	private static final long NUMERIC_ADDR = addressOf(NUMERIC);
	static {
		byte[] ignored = "\t\n\r :,".getBytes();
		for (int i = 0; i < ignored.length; i++)
			IGNORE.put(ignored[i], (byte) 1);
		byte[] numeric = "+-.eE0123456789".getBytes();
		for (int i = 0; i < numeric.length; i++)
			NUMERIC.put(numeric[i], (byte) 1);
	}
	private int pos = -1;

	@SuppressWarnings("restriction")
	private static boolean ignore(byte b) {
		return UNSAFE.getByte(null, IGNORE_ADDR + (b & 0xFF)) == 1;
	}

	@SuppressWarnings("restriction")
	private static boolean isNumberPart(byte b) {
		return UNSAFE.getByte(null, NUMERIC_ADDR + (b & 0xFF)) == 1;
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