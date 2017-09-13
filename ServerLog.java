package test.server;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ServerLog {
	String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
	
	String logFile = "C://log//chat_log_" + today + ".log";

	FileWriter fw;
	static final String ENTER = System.getProperty("line.separator");

	public ServerLog() {
		try {
			fw = new FileWriter(logFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void debug(String msg) {
		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		try {
			fw.write(time + " : ");
			fw.write(msg + ENTER);
			fw.flush();
		} catch (IOException e) {
			System.err.println("IOException!");
		}
	}
}