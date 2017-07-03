#include <jni.h>

static inline char ignore(const char c) {
	if (c < '\t' || c > ':') return 0;
	static const char res[] = {1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
	return res[c-'\t'];
}

static inline char isescaped(const char* input, int pos) {
	int p = pos;
	char c = 0;
	while (input[--p] == '\\') c ^= 1;
	return c;
}

static int json(const char* input, int* out) {
	static const void* jmp[] = {
	&&_str, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_com,
	&&_num, &&_d, &&_d,
	&&_num, &&_num,&&_num,&&_num,&&_num,&&_num,&&_num,&&_num,&&_num,&&_num,
	&&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_barr, &&_d,
	&&_earr, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_f, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_n, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_t, &&_d, &&_d, &&_d, &&_d, &&_d, &&_d,
	&&_bobj, &&_d,
	&&_eobj};
	static const char num[] = {
	1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
	int stacklen = 0, pos = -1, start, outpos = 0;
	char inentry = 0;
	int stack = 0;
	do {
		char c;
		while (ignore(c = input[++pos]));
		if (c == '\0') return pos;
		goto *jmp[c-'"'];

_str:	start = pos + 1;
		while (input[++pos] != '"' || isescaped(input, pos));
		if (inentry) {
			out[outpos++] = 1;
			out[outpos++] = start;
			out[outpos++] = pos - start;
			inentry = 0;
		} else {
			out[outpos++] = 2;
			out[outpos++] = start;
			out[outpos++] = pos - start;
		}
		continue;

_com:	inentry = !(stack & (1 << stacklen));
		continue;

_num:	start = pos;
		do {
			c = input[++pos];
		} while (c >= '+' && c <= 'e' && num[c-'+']);
		out[outpos++] = 3;
		out[outpos++] = start;
		out[outpos++] = pos-- - start;
		continue;

_bobj:	inentry = 1;
		out[outpos++] = 4;
		stacklen++;
		stack &= ~(1 << stacklen);
		continue;

_eobj:	stacklen--;
		out[outpos++] = 5;
		continue;

_barr:	stacklen++;
		stack |= 1 << stacklen;
		out[outpos++] = 6;
		continue;

_earr:	stacklen--;
		out[outpos++] = 7;
		continue;

_f:		pos += 4;
		out[outpos++] = 8;
		continue;

_n:		pos += 3;
		out[outpos++] = 9;
		continue;

_t:		pos += 3;
		out[outpos++] = 10;
		continue;
_end:
		continue;
_d:
		return -1;
	} while (stacklen > 0);
	return outpos;
}

JNIEXPORT jint JNICALL Java_ftljson_JNI_json(JNIEnv* env, jclass clazz, long input, long output) {
	const char* str = (const char*)(intptr_t)input;
	int* out = (int*)(intptr_t)output;
	return (jint) json(str, out);
}
