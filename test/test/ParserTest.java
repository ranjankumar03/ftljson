package test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ftljson.*;

@RunWith(Parameterized.class)
public class ParserTest {

	@Parameters(name = "{0}")
	public static Object[][] provideTestData() {
		File[] testFiles = new File("./dataset").listFiles((dir, name) -> 
			name.endsWith(".json")
		);
		Object[][] res = new Object[testFiles.length][];
		for (int i = 0; i < testFiles.length; i++)
			res[i] = new Object[] { testFiles[i] };
		return res;
	}

	@Parameter
	public File file;

	private ToStringParseListener listener = new ToStringParseListener();

	private byte[] input() throws Exception {
		InputStream is = new FileInputStream(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int r;
		while ((r = is.read(buff, 0, buff.length)) != -1)
			baos.write(buff, 0, r);
		is.close();
		return baos.toByteArray();
	}

	private void assertCorrect(Consumer<byte[]> test) throws Exception {
		byte[] input = input();
		JsonParserKS refParser = new JsonParserKS();
		ToStringParseListener refListener = new ToStringParseListener();
		refParser.json(input, refListener);
		String expected = refListener.toString();
		test.accept(input);
		assertEquals(expected, refListener.toString());
	}

	@Test
	public void ks15() throws Exception {
		assertCorrect((input) -> JsonParserKS15.json(input, listener));
	}

	@Test
	public void ks14() throws Exception {
		assertCorrect((input) -> JsonParserKS14.json(input, listener));
	}

	@Test
	public void kss() throws Exception {
		assertCorrect((input) -> JsonParserKSS.json(input, listener));
	}

	@Test
	public void ks9() throws Exception {
		assertCorrect((input) -> new JsonParserKS9().json(input, listener));
	}

	@Test
	public void ks8() throws Exception {
		assertCorrect((input) -> new JsonParserKS8().json(input, listener));
	}

	@Test
	public void ks7() throws Exception {
		assertCorrect((input) -> new JsonParserKS7().json(input, listener));
	}

	@Test
	public void ks6() throws Exception {
		assertCorrect((input) -> new JsonParserKS6().json(input, listener));
	}

	@Test
	public void ks5() throws Exception {
		assertCorrect((input) -> new JsonParserKS5().json(input, listener));
	}

	@Test
	public void ks4() throws Exception {
		assertCorrect((input) -> new JsonParserKS4().json(input, listener));
	}

	@Test
	public void ks3() throws Exception {
		assertCorrect((input) -> new JsonParserKS3().json(input, listener));
	}

	@Test
	public void ks2() throws Exception {
		assertCorrect((input) -> new JsonParserKS2().json(input, listener));
	}

}
