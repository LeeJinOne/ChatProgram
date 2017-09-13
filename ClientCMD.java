package test.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class ClientCMD {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private TClientCMD2 cmd;

	private String id, myMsg;

	private Scanner scan = new Scanner(System.in);

	public ClientCMD() {
		connect();
		MessageHandler mHandler = new MessageHandler(socket);
		id = mHandler.id;
		mHandler.start();

		while(true) {
			myMsg = scan.nextLine();

			if (myMsg.equalsIgnoreCase("-exit")){
				JSONObject jsonObject2 = new JSONObject();

				jsonObject2.put("userID", id);
				jsonObject2.put("body", myMsg);

				String exitMessage = jsonObject2.toJSONString();

				sendMessage(exitMessage);
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
				break;
			} else {
				JSONObject jsonObject = new JSONObject();

				jsonObject.put("userID", id);
				jsonObject.put("body", myMsg);

				String message = jsonObject.toJSONString();

				sendMessage(message);
			}
		}
	}

	public static void main(String[] args) {
		new TClientCMD2();
	}

	public void connect() {
		try {
			socket = new Socket("127.0.0.1", 5432);
			System.out.println("Succeed connect to server" + "\n" + "=========================");

			out = new DataOutputStream(socket.getOutputStream());

		} catch (IOException e) {
				e.printStackTrace();
		}
	}

	public void sendMessage(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class MessageHandler extends Thread {
		Socket socket;
		String id;
		DataInputStream in;

		public MessageHandler(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());

				id = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		public void run() {
			try {
				while(in != null){
					String msg = in.readUTF();
					System.out.println(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}