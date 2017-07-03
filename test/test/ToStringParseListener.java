package test;

import ftljson.ParseListener;

public class ToStringParseListener implements ParseListener {
	private final StringBuilder sb = new StringBuilder();

	@Override
	public void stringLiteral(int off, int len) {
		sb.append("SL").append(off).append(len);
	}

	@Override
	public void numberLiteral(int off, int len) {
		sb.append("NL").append(off).append(len);
	}

	@Override
	public void endObject() {
		sb.append("}");
	}

	@Override
	public void endList() {
		sb.append("]");
	}

	@Override
	public void booleanLiteral(boolean value) {
		sb.append("BL").append(value ? "TRUE" : "FALSE");
	}

	@Override
	public void beginObjectEntry(int off, int len) {
		sb.append(":").append(off).append(len);
	}

	@Override
	public void beginObject() {
		sb.append("{");
	}

	@Override
	public void beginList() {
		sb.append("[");
	}

	@Override
	public void nullLiteral() {
		sb.append("NL");
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	public void reset() {
		sb.setLength(0);
	}
}
