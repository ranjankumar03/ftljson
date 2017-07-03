package bench;

import ftljson.*;

import org.noggit.JSONParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class B {

	@Param({ "menu", "menu2", "escaped", "card", "json2" })
	private String file;

	private ParseListener NULL = new ParseAdapter();
	private byte[] input;
	private char[] chars; // <- for noggit
	private ByteBuffer bb;
	private ByteBuffer eventsTmp;

	@Setup
	public void setup() {
		try {
			InputStream is = new FileInputStream(new File("./dataset/" + file + ".json"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int r;
			while ((r = is.read(buff, 0, buff.length)) != -1)
				baos.write(buff, 0, r);
			input = baos.toByteArray();
			is.close();
			chars = new String(input, 0, input.length).toCharArray();
			bb = ByteBuffer.allocateDirect(input.length).order(ByteOrder.nativeOrder());
			bb.put(input);
			bb.rewind();
			eventsTmp = ByteBuffer.allocateDirect(input.length * 4).order(ByteOrder.nativeOrder());
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	// @Benchmark
	public void ks(Blackhole bh) throws Exception {
		JsonParserKS parser = new JsonParserKS();
		bh.consume(parser.json(input, NULL));
	}

	// @Benchmark
	public void ks2(Blackhole bh) throws Exception {
		JsonParserKS2 parser = new JsonParserKS2();
		bh.consume(parser.json(input, NULL));
	}

	// @Benchmark
	public void ks3(Blackhole bh) throws Exception {
		JsonParserKS3 parser = new JsonParserKS3();
		bh.consume(parser.json(input, NULL));
	}

	// @Benchmark
	public void ks4(Blackhole bh) throws Exception {
		JsonParserKS4 parser = new JsonParserKS4();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks5(Blackhole bh) throws Exception {
		JsonParserKS5 parser = new JsonParserKS5();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks6(Blackhole bh) throws Exception {
		JsonParserKS6 parser = new JsonParserKS6();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks7(Blackhole bh) throws Exception {
		JsonParserKS7 parser = new JsonParserKS7();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks8(Blackhole bh) throws Exception {
		JsonParserKS8 parser = new JsonParserKS8();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks9(Blackhole bh) throws Exception {
		JsonParserKS9 parser = new JsonParserKS9();
		bh.consume(parser.json(input, NULL));
	}

	@Benchmark
	public void ks14(Blackhole bh) throws Exception {
		bh.consume(JsonParserKS14.json(input, NULL));
	}

	@Benchmark
	public void ks15(Blackhole bh) throws Exception {
		bh.consume(JsonParserKS15.json(input, NULL));
	}

	@Benchmark
	public void kss(Blackhole bh) throws Exception {
		bh.consume(JsonParserKSS.json(input, NULL));
	}

	//@Benchmark
	public void jni(Blackhole bh) throws Exception {
		JNI.json(bb, eventsTmp, NULL);
	}

	@Benchmark
	public void noggit(Blackhole bh) throws Exception {
		JSONParser p = new JSONParser(chars, 0, chars.length);
		p.setFlags(JSONParser.FLAGS_STRICT);
		int i = 0, e;
		while ((e = p.nextEvent()) != JSONParser.EOF)
			i += e;
		bh.consume(i);
	}

	@Benchmark
	public void jackson(Blackhole bh) throws Exception {
		JsonFactory f = new JsonFactory();
		JsonParser p = f.createParser(input);
		int i = 0;
		while (p.nextToken() != null) {
			i++;
		}
		bh.consume(i);
	}

	@Benchmark
	public void gson(Blackhole bh) throws Exception {
		JsonReader reader = new JsonReader(new CharArrayReader(chars));
		while (true) {
			JsonToken token = reader.peek();
			switch (token) {
			case BEGIN_ARRAY:
				reader.beginArray();
				break;
			case END_ARRAY:
				reader.endArray();
				break;
			case BEGIN_OBJECT:
				reader.beginObject();
				break;
			case END_OBJECT:
				reader.endObject();
				break;
			case NAME:
				reader.nextName();
				break;
			case STRING:
				reader.nextString();
				break;
			case NUMBER:
				reader.nextString();
				break;
			case BOOLEAN:
				reader.nextBoolean();
				break;
			case NULL:
				reader.nextNull();
				break;
			case END_DOCUMENT:
				reader.close();
				return;
			}
		}
	}

	public static void main(String[] args) throws RunnerException {
		new Runner(new OptionsBuilder().include(B.class.getSimpleName()).build()).run();
	}

}
