package com.owlike.genson.stream;

import java.io.IOException;

public class DoubleParseAlgorithmTest {
	public static void main(String[] args) throws IOException {
		for (int p = 1; p < 23; p++) {
			alllong(p, new char[27], 0);
			System.out.println("passed " + p);
		}
	}

	static void alllong(int pow, char[] arr, int pos) throws IOException {
		if (pos > 20) return;

		for (int i = 0; i < 10; i++) {
			arr[pos] = (char) (i+48);

			arr[pos + 1] = 'E';
			arr[pos + 2] = '-';
			String ps = ""+pow;
			for (int k = 0; k < ps.length(); k++)
				arr[pos+k+3] = ps.charAt(k); 

			String s = new String(arr, 0, pos + 3 + ps.length());
			try {
				double d = Double.parseDouble(s);
				JsonReader reader = new JsonReader(s);
				double d2 = reader.valueAsDouble();
				reader.close();
				if (d != d2) System.out.println("d=" + d + ", d2=" + d2 + ", str=" + s);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("ex for str=" + s);
			}

			alllong(pow, arr, pos + 1);
		}
	}
}
