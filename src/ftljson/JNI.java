package ftljson;

import static ftljson.Unsafe.UNSAFE;
import static ftljson.Unsafe.addressOf;

import java.nio.ByteBuffer;

@SuppressWarnings("restriction")
public class JNI {

	static {
		//Runtime.getRuntime().loadLibrary("native/ftljson");
	}

	public static final native int json(long input, long output);

	public static void json(ByteBuffer input, ByteBuffer eventsTmp, ParseListener listener) {
		long addr = addressOf(eventsTmp);
		int numevents = json(addressOf(input), addr);
		if (numevents == -1) return;
		int pos = 0;
		while (numevents --> 0) {
			int type = UNSAFE.getInt(null, addr + ((pos++) << 2));
			switch (type) {
			case 1:
				listener.beginObjectEntry(UNSAFE.getInt(null, addr + ((pos++) << 2)), UNSAFE.getInt(null, addr + ((pos++) << 2)));
				break;
			case 2:
				listener.stringLiteral(UNSAFE.getInt(null, addr + ((pos++) << 2)), UNSAFE.getInt(null, addr + ((pos++) << 2)));
				break;
			case 3:
				listener.numberLiteral(UNSAFE.getInt(null, addr + ((pos++) << 2)), UNSAFE.getInt(null, addr + ((pos++) << 2)));
				break;
			case 4:
				listener.beginObject();
				break;
			case 5:
				listener.endObject();
				break;
			case 6:
				listener.beginList();
				break;
			case 7:
				listener.endList();
				break;
			case 8:
				listener.booleanLiteral(false);
				break;
			case 9:
				listener.nullLiteral();
				break;
			case 10:
				listener.booleanLiteral(true);
				break;
			default:
				return;
			}
		}
	}

}
