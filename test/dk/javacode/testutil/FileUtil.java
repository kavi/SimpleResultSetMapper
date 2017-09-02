package dk.javacode.testutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
	/**
	 * Return the content of the file as a string.
	 * 
	 * @param filename File to read.
	 * @return the content of the file as a string.
	 */
	public String readFile(String filename) {
		StringBuilder createSql = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filename)));
			while (br.ready()) {
				createSql.append(br.readLine() + "\n");
			}
			return createSql.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file: " + filename, e);
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				throw new RuntimeException("Unable to close " + filename, e);
			}
		}
	}
}
