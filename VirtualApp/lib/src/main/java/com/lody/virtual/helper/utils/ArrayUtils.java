package com.lody.virtual.helper.utils;

import com.lody.virtual.helper.compat.ObjectsCompat;

/**
 * @author Lody
 *
 */
public class ArrayUtils {

	public static <T> boolean contains(T[] array, T value) {
		return indexOf(array, value) != -1;
	}
	public static boolean contains(int[] array, int value) {
		if (array == null) return false;
		for (int element : array) {
			if (element == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return first index of {@code value} in {@code array}, or {@code -1} if
	 * not found.
	 */
	public static <T> int indexOf(T[] array, T value) {
		if (array == null) return -1;
		for (int i = 0; i < array.length; i++) {
			if (ObjectsCompat.equals(array[i], value)) return i;
		}
		return -1;
	}

	public static int protoIndexOf(Class<?>[] array, Class<?> type) {
		if (array == null) return -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == type) return i;
		}
		return -1;
	}

	public static int indexOfFirst(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && isClass(one, type)) {
					return N;
				}
			}
		}
		return -1;
	}

	public static int protoIndexOf(Class<?>[] array, Class<?> type, int sequence) {
		if (array == null) {
			return -1;
		}
		while (sequence < array.length) {
			if (type == array[sequence]) {
				return sequence;
			}
			sequence++;
		}
		return -1;
	}


	public static int indexOfObject(Object[] array, Class<?> type, int sequence) {
		if (array == null) {
			return -1;
		}
		while (sequence < array.length) {
			if (type.isInstance(array[sequence])) {
				return sequence;
			}
			sequence++;
		}
		return -1;
	}

	public static void dumpArrayType(Object[] array) {
		if (array == null) {
			return ;
		}
		int sequence = 0;
		while (sequence < array.length) {
			VLog.logbug(VLog.VTAG, "Type["+sequence+"]:" + array[sequence] == null ? "null" : array[sequence].getClass().getName());
			sequence++;
		}
	}

	public static int indexOf(Object[] array, Class<?> type, int sequence) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && isClass(one, type)) {
					if (--sequence <= 0) {
						return N;
					}
				}
			}
		}
		return -1;
	}

	public static int indexOfLast(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			for (int N = array.length; N > 0; N--) {
				Object one = array[N - 1];
				if (one != null && isClass(one,type)) {
					return N - 1;
				}
			}
		}
		return -1;
	}

	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFirst(Object[] args, Class<?> clazz) {
		int index = indexOfFirst(args, clazz);
		if (index != -1) {
			return (T) args[index];
		}
		return null;
	}

	public static void checkOffsetAndCount(int arrayLength, int offset, int count) throws ArrayIndexOutOfBoundsException {
		if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
			throw new ArrayIndexOutOfBoundsException(offset);
		}
	}

	public static boolean isClass(Object obj, Class type)
	{
		boolean retValue = false;
		try
		{
			;
			for(Class clzz = obj.getClass(); clzz != null; clzz = clzz.getSuperclass())
			{
				if(clzz.getName().equals(type.getName()))
				{
					retValue = true;
					return retValue;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return retValue;

	}
}
