package Zeze.Util;

import Zeze.*;
import java.util.*;

public final class Random {
	private static java.util.Random Instance = new java.util.Random();
	public static java.util.Random getInstance() {
		return Instance;
	}

	public static <T> List<T> Shuffle(List<T> list) {
		for (int i = 1; i < list.size(); i++) {
			int pos = getInstance().nextInt(i + 1);
			var x = list.get(i);
			list.set(i, list.get(pos));
			list.set(pos, x);
		}
		return list;
	}

	public static <T> T[] Shuffle(T[] list) {
		for (int i = 1; i < list.length; i++) {
			int pos = getInstance().nextInt(i + 1);
			var x = list[i];
			list[i] = list[pos];
			list[pos] = x;
		}
		return list;
	}
}