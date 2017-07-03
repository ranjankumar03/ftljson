package ftljson;

import java.nio.Buffer;

@SuppressWarnings("restriction")
public final class Unsafe {

	public static final sun.misc.Unsafe UNSAFE = getUnsafeInstance();
	private static final long ADDRESS = findBufferAddress();

	private Unsafe() {
	}

	private static sun.misc.Unsafe getUnsafeInstance() {
		java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();
		for (java.lang.reflect.Field field : fields) {
			if (!field.getType().equals(sun.misc.Unsafe.class)) {
				continue;
			}
			int modifiers = field.getModifiers();
			if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
				continue;
			}
			field.setAccessible(true);
			try {
				return (sun.misc.Unsafe) field.get(null);
			} catch (IllegalAccessException e) {
			}
			break;
		}
		throw new UnsupportedOperationException();
	}

	private static final java.lang.reflect.Field getDeclaredField(Class<?> root, String fieldName)
			throws NoSuchFieldException {
		Class<?> type = root;
		do {
			try {
				java.lang.reflect.Field field = type.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			} catch (SecurityException e) {
				type = type.getSuperclass();
			}
		} while (type != null);
		throw new NoSuchFieldException(
				fieldName + " does not exist in " + root.getName() + " or any of its superclasses.");
	}

	private static long findBufferAddress() {
		try {
			return UNSAFE.objectFieldOffset(getDeclaredField(Buffer.class, "address"));
		} catch (Exception e) {
			throw new UnsupportedOperationException("Could not detect ByteBuffer.address offset", e);
		}
	}

	public static final long addressOf(Buffer buffer) {
		return UNSAFE.getLong(buffer, ADDRESS);
	}

}
