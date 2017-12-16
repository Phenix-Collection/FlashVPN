package com.polestar.domultiple.utils;

import java.io.UnsupportedEncodingException;

public class HexUtils {
	private static final char[] DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
			'd', 'e', 'f' };

	private static final byte[] EMPTY_BYTES = new byte[0];

	private static final String EMPTY_STR = "";

	/**
	 * @return String Hex String
	 */
	public static String byte2HexStr(byte b) {
		char[] buf = new char[2];
		buf[1] = DIGITS[b & 0xF];
		b = (byte) (b >>> 4);
		buf[0] = DIGITS[b & 0xF];
		return new String(buf);
	}

	/**
	 * @param bytes
	 * @return String
	 */
	public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return EMPTY_STR;
        }

        char[] buf = new char[2 * bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            buf[2 * i + 1] = DIGITS[b & 0xF];
            b = (byte) (b >>> 4);
            buf[2 * i + 0] = DIGITS[b & 0xF];
        }
        return new String(buf);
    }

	/**
	 * @return byte
	 */
	public static byte hexStr2Byte(String hexStr) {
		if (hexStr != null && hexStr.length() == 1) {
			return char2Byte(hexStr.charAt(0));
		} else {
			return 0;
		}
	}

	/**
	 * @return byte
	 */
	public static byte char2Byte(char ch) {
		if (ch >= '0' && ch <= '9') {
			return (byte) (ch - '0');
		} else if (ch >= 'a' && ch <= 'f') {
			return (byte) (ch - 'a' + 10);
		} else if (ch >= 'A' && ch <= 'F') {
			return (byte) (ch - 'A' + 10);
		} else {
			return 0;
		}
	}

	public static byte[] hexStr2Bytes(String hexStr) {
		if (hexStr == null || hexStr.equals("")) {
			return EMPTY_BYTES;
		}

		byte[] bytes = new byte[hexStr.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			char high = hexStr.charAt(i * 2);
			char low = hexStr.charAt(i * 2 + 1);
			bytes[i] = (byte) (char2Byte(high) * 16 + char2Byte(low));
		}
		return bytes;
	}

	/**
	 * @param hexStr
	 * @return String
	 */
	public static String hexStr2Str(String hexStr) {
		byte[] bytes = HexUtils.hexStr2Bytes(hexStr);
		String str = new String(bytes);
		return str;
	}

	public static String str2HexStr(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		byte[] temp = str.getBytes();
		String hexStr = HexUtils.bytes2HexStr(temp);
		return hexStr;
	}
	
   public static String str2HexStr(String str, String charset) {
        if (str == null || str.length() == 0) {
            return str;
        }
        byte[] temp = null;
        try {
            temp = str.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String hexStr = HexUtils.bytes2HexStr(temp);
        return hexStr;
    }
	
	public static void printHexStr(String tag, byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder(55 + 30 + (bytes.length + 15) / 16 * 73);
		sb.append("buf size: " + bytes.length).append("\r\n");
		sb.append("______00_01_02_03_04_05_06_07_08_09_0A_0B_0C_0D_0E_0F\r\n");
		StringBuilder sbfr = new StringBuilder(32);
		sbfr.append(" ");
		for (int i = 0, x = 0, r = 0;; i++) {
			if (i < bytes.length) {
				if (x == 0) {
					sb.append(String.format("%04x: ", i));
				}
				sb.append(String.format("%02x ", bytes[i]));
				int b = bytes[i] & 0xff;
				if (b >= 0x20 && b <= 0x7f) {
					sbfr.append(String.format("%c", bytes[i]));
				} else {
					sbfr.append(".");
				}
			} else {
				if (x == 0) {
					break;
				}
				sb.append("   ");
				sbfr.append(" ");
				r = 1;
			}
			x++;
			if (x >= 16) {
				sb.append(sbfr.toString()).append("\r\n");
				sbfr.setLength(1);
				x = 0;
				if (r != 0) {
					break;
				}
			}
		}
	}
}