package ftljson;

public interface ParseListener {
    void beginObject();
    void endObject();
    void booleanLiteral(boolean value);
    void numberLiteral(int off, int len);
    void stringLiteral(int off, int len);
    void nullLiteral();
    void beginObjectEntry(int off, int len);
    void beginList();
    void endList();
}
