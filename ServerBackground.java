package test.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.sound.midi.Receiver;

import test.server.ServerLog;

public class ServerBackground {
	private ServerSocket serverSocket;
	private Socket socket;
	private ServerGUI gui;
	private ServerLog log = new ServerLog();

	private JSONObject jsonObject = new JSONObject();
	private JSONParser parser = new JSONParser();

	private Map<String, DataOutputStream> clientMap = new HashMap<String, DataOutputStream>();
	public Map<String, String> conMap = new HashMap<String, String>();

	private DataOutputStream out;

	private final String serverId = "Server";
	private String ioTime = "";
	private static String id, msg;
	private static int num = 0;
	private String userList = "";

	public void getTime() {
		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		ioTime = time;
	}

	public void setID() {
		String nick = "user" + num++;
		id = nick;
	}

	public void setGui(TServerGUI gui) {
		this.gui = gui;
	}

	public static void main(String[] args) {
		ServerBackground sb = new ServerBackground();
		sb.setting();
		sb.writeLog();
	}

	public void setting(){
		try {
			Collections.synchronizedMap(clientMap); // HashMap being network
			Collections.synchronizedMap(conMap); // Connected client list map
			serverSocket = new ServerSocket(5432); // Setting Server socket port
			
			while (true) {
				System.out.println("Wait for connect...");
				socket = serverSocket.accept(); // Accept user socket be connected

				System.out.println("Connect from " + socket.getInetAddress()); // Connect IP

				Receiver receiver = new Receiver(socket); // Make Receiver for each user
				receiver.start(); // Start thread
				viewUserList(serverId);
				gui.listArea.setText(userList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addClient(String id, DataOutputStream out) throws IOException {
		getTime(); // Get current time
		String message = id + " is login at " + ioTime;
		clientMap.put(id, out); // Put ID and DataOutputStream in clientMap
		systemMsg(message); // Send Message to all user
		conMap.put(id, ioTime); // Put ID and current time in conMap(for connected user list map)
		log.debug(message);
	}

	public void removeClient(String id){
		getTime();
		String message = id + " is logout at " + ioTime;
		systemMsg(message);
		gui.appendMsg(message);
		clientMap.remove(id); // Remove user from clientMap
		conMap.remove(id); // Remove user from conMap
		try{
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.debug(message);
	}

	public void messageToAll(String id, String msg) {
		Iterator<String> iterator = clientMap.keySet().iterator();
		String key = "", message="";
		while(iterator.hasNext()) {
			key = iterator.next();
			try{
				message = id + " : " + msg;
				clientMap.get(key).writeUTF(message);
			} catch (IOException e) {
				System.out.println("Failed to messageToAll method");
				e.printStackTrace();
			}
		}
		gui.appendMsg(message);
		log.debug(message);
	}

	public void messageToOne(String id, String target, String msg) {
		Iterator<String> iterator = clientMap.keySet().iterator();
		String key = "", mTOmessage="";
		while(iterator.hasNext()) {
			key = iterator.next();
			if(key.equalsIgnoreCase(target)) {
				try{
					String message = "Whisper from " + id + " : " + msg;
					clientMap.get(target).writeUTF(message);

					String myMessage = "Whisper to " + target + " : " + msg;
					clientMap.get(id).writeUTF(myMessage);

					mTOmessage = "[MTO] From" + id + " to " + target + " : " + msg;
				} catch (IOException e) {
					System.out.println("Failed to messageToOne method");
					e.printStackTrace();
				}
				System.out.println("mTO method proceed");
			}
		}
		gui.appendMsg(mTOmessage);
		log.debug(mTOmessage);
	}
	
	/* 메시지를 받아서 타입에 따라 분류한다. */
	public void receiveMessage(String msg) { 
		String id="", message="", msgType="", userNum="", mtoMsg="";
		try {
			jsonObject = (JSONObject)parser.parse(msg);

			id = (String) jsonObject.get("userID").toString();
			message = (String) jsonObject.get("body").toString();
			System.out.println("receiveMessage - parsing jsonObject");

			if (message.length() > 6) {
				msgType = message.substring(1, 5); // Find 'user'
				System.out.println("msgType : " + msgType);
				userNum = message.substring(5, 6); // If 'user',
				System.out.println("userNum : " + userNum);
				mtoMsg = message.substring(7); // Make massager
				System.out.println("mtoMsg : " + mtoMsg);
			} else {
				msgType = message.substring(1, message.length()); // Find 'exit, list'
				System.out.println("msgType : " + msgType);
			}
			System.out.println("finish substring");
		} catch (ParseException ex) {
			System.out.println("Failed to parsing jsonObejct!!!");
			log.debug("Failed to parsing jsonObejct!!!");
			ex.printStackTrace();
		}

		if (message.charAt(0) == '-') {
			System.out.println("if statement");

			if (msgType.equalsIgnoreCase("user")) {
				System.out.println("receiveMessage - messageToOne");
				String target = msgType + userNum;
				messageToOne(id, target, mtoMsg);
			} else if (msgType.equalsIgnoreCase("exit")) {
				System.out.println("receiveMessage - removeClient");
				removeClient(id);
			} else if (msgType.equalsIgnoreCase("list")) {
				String list ="";
				viewUserList(id);
			} else {
				System.out.println("receiveMessage - messageToAll");
				messageToAll(id, message);
			}
		} else {
			System.out.println("receiveMessage<else> - messageToAll");
			messageToAll(id, message);
		}
	}

	public void viewUserList (String id) {
		Iterator<String> iterator = conMap.keySet().iterator();
		String key = "", list="";
		list = "**Current connected User List** \n";
		while(iterator.hasNext()) {
			key = iterator.next();
			list += key + " / " + conMap.get(key) + "\n";
		}
		list += "-------------------------------- \n";
		if (id == serverId) {
			this.userList = list;
		} else {
			try{
				clientMap.get(id).writeUTF(list);
				log.debug(id + " called connected user list");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void systemMsg(String msg) {
		Iterator<String> iterator = clientMap.keySet().iterator();
		String key = "" , message ="";
		while(iterator.hasNext()) {
			key = iterator.next();
			try{
				message = "<System>" + msg;
				clientMap.get(key).writeUTF(message);	
			} catch (IOException e) {
				System.out.println("Failed to systemMsg method");
				e.printStackTrace();
			}
		}
		gui.appendMsg(message);
		log.debug(message);
	}

	public void wellcomeMsg (String id) {
		Iterator<String> iterator = clientMap.keySet().iterator();
		String key = "";
		String list = "";
		while (iterator.hasNext()) {
			key = iterator.next();
			if(key == id){
				try {
					String wcMsg = "[Wellcome] Your ID : " + id + "  / Connect Time : " + conMap.get(key) + "\n";
					clientMap.get(key).writeUTF(wcMsg);
					viewUserList(id);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void writeLog() {
		System.out.println("Write Log Start");
		
		log.debug("Writing Log Start");
		log.close();
	}

	/* *************** RECEIVER **************** */
	class Receiver extends Thread {
		private DataInputStream in;
		private DataOutputStream out;
		private String id;
		private ServerBackground sb = new ServerBackground();

		public Receiver(Socket socket) {
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				setID();
				id = sb.id;
				out.writeUTF(id);
				addClient(id, out);
				wellcomeMsg(id);
				log.debug(id + "is connect");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (in != null) {
					msg = in.readUTF();
					receiveMessage(msg);
				}
			} catch (Exception e) {

			}
		}
	}
}