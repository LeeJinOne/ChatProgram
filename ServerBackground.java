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

import test.server.TServerLog;

public class TServerBackground {
	private ServerSocket serverSocket;
	private Socket socket;
	private TServerGUI gui;
	private TServerLog log = new TServerLog();

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
		TServerBackground tsb = new TServerBackground();
		tsb.setting();
		tsb.writeLog();
	}

	public void setting(){
		try {
			Collections.synchronizedMap(clientMap); // HashMap 네트워크 화
			Collections.synchronizedMap(conMap); // 접속 목록 맵
			serverSocket = new ServerSocket(5432); // 서버 소켓 할당
			
			while (true) {
				System.out.println("Wait for connect...");
				socket = serverSocket.accept(); // 유저 소켓에게 접속 허락

				System.out.println("Connect from " + socket.getInetAddress()); // 접속 IP

				Receiver receiver = new Receiver(socket); // 유저 소켓에 리시버 할당
				receiver.start(); // 리시버 시작
				viewUserList(serverId);
				gui.listArea.setText(userList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addClient(String id, DataOutputStream out) throws IOException {
		getTime(); // 현재시간 가져오기
		String message = id + "님이 접속하셨습니다. " + ioTime;
		clientMap.put(id, out); // 유저맵에 객체 등록
		systemMsg(message); // 모두에게 메세지 전송
		conMap.put(id, ioTime); // 접속 목록에 아이디와 접속시간 등록
		log.debug(message);
	}

	public void removeClient(String id){
		getTime();
		String message = id + "님이 접속을 종료하셨습니다. " + ioTime;
		systemMsg(message);
		gui.appendMsg(message);
		clientMap.remove(id); // 유저맵에 객체 삭제
		conMap.remove(id); // 접속 목록에서 삭제
		try{
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.debug(message);
	}

	public void messageToAll(String id, String msg) {
		Iterator<String> iterator = clientMap.keySet().iterator(); // 유저맵에서 유저의 키값 전부 찾음
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
					String message = id + "님의 귓속말 : " + msg;
					clientMap.get(target).writeUTF(message);

					String myMessage = target + "에게 귓속말 : " + msg;
					clientMap.get(id).writeUTF(myMessage);

					mTOmessage = id + "가 " + target + "에게 귓속말 : " + msg;
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
				msgType = message.substring(1, 5); // 'user' 찾기
				System.out.println("msgType : " + msgType);
				userNum = message.substring(5, 6); // 만약 'user'라면 몇번 유저를 찾는건지
				System.out.println("userNum : " + userNum);
				mtoMsg = message.substring(7); // 'user'면 7번째 문자부터 추출해서 메세지 만들기
				System.out.println("mtoMsg : " + mtoMsg);
			} else {
				msgType = message.substring(1, message.length()); // 'exit, list' 찾기
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
		list = "**현재 접속중인 클라이언트 목록** \n";
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
				log.debug(id + "가 접속 목록 호출");
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
					String wcMsg = "[환영합니다] 당신의 아이디 : " + id + "  / 접속시간 : " + conMap.get(key) + "\n";
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
		private TServerBackground tsb = new TServerBackground();

		public Receiver(Socket socket) {
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				setID();
				id = tsb.id;
				out.writeUTF(id);
				addClient(id, out);
				wellcomeMsg(id);
				log.debug(id + "에게 ID부여 및 리시버 할당");
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