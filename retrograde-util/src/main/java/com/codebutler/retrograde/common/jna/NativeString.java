package com.codebutler.retrograde.common.jna;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.CharBuffer;

/** Provides a temporary allocation of an immutable C string
 * (<code>const char*</code> or <code>const wchar_t*</code>) for use when
 * converting a Java String into a native memory function argument.
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author twall@users.sf.net
 *
 * https://stackoverflow.com/questions/10158582/converting-string-to-pointer-for-jna
 */
@SuppressWarnings({"WeakerAccess", "NullableProblems"})
public class NativeString implements CharSequence, Comparable {

    private Pointer pointer;
    private boolean wide;

    /** Create a native string (NUL-terminated array of <code>char</code>).<p>
     * If the system property <code>jna.encoding</code> is set, its value will
     * be used to encode the native string.  If not set or if the encoding
     * is unavailable, the default platform encoding will be used.
     */
    public NativeString(String string) {
        this(string, false);
    }

    /** Create a native string as a NUL-terminated array of <code>wchar_t</code>
     * (if <code>wide</code> is true) or <code>char</code>.<p>
     * If the system property <code>jna.encoding</code> is set, its value will
     * be used to encode the native <code>char</code>string.
     * If not set or if the encoding is unavailable, the default platform
     * encoding will be used.
     *
     * @param string value to write to native memory
     * @param wide whether to store the String as <code>wchar_t</code>
     */
    public NativeString(String string, boolean wide) {
        if (string == null) {
            throw new NullPointerException("String must not be null");
        }
        // Allocate the memory to hold the string.  Note, we have to
        // make this 1 element longer in order to accommodate the terminating
        // NUL (which is generated in Pointer.setString()).
        this.wide = wide;
        if (wide) {
            int len = (string.length() + 1 ) * Native.WCHAR_SIZE;
            pointer = new Memory(len);
            pointer.setWideString(0, string);
        }
        else {
            byte[] data = Native.toByteArray(string);
            pointer = new Memory(data.length + 1);
            pointer.write(0, data, 0, data.length);
            pointer.setByte(data.length, (byte)0);
        }
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof CharSequence && compareTo(other) == 0;
    }

    public String toString() {
        if (wide) {
            return String.format("const wchar_t*(%s)", pointer.getWideString(0));
        } else {
            return String.format("const char*(%s)", pointer.getString(0));
        }
    }

    public Pointer getPointer() {
        return pointer;
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public int length() {
        return toString().length();
    }

    public CharSequence subSequence(int start, int end) {
        return CharBuffer.wrap(toString()).subSequence(start, end);
    }

    public int compareTo(Object other) {
        if (other == null) {
            return 1;
        }
        return toString().compareTo(other.toString());
    }
}
