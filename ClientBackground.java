package test.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;

public class ClientBackground {
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	private ClientGUI gui;

	private String msg;
	private String id;

	private JSONObject jsonObject = new JSONObject();

	public void setGui(ClientGUI gui) {
		this.gui = gui;
	}

	public static void main(String[] args) {
		ClientBackground cb = new ClientBackground();
		cb.connect();
	}

	public void connect() {
		try {
			socket = new Socket("127.0.0.1", 5432);
			System.out.println("Succeed connect to server");

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			id = in.readUTF();
			gui.getID(id);

			while(in != null) {
				msg = in.readUTF();
				gui.appendMsg(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reconnect() {
		try {
			socket = new Socket("127.0.0.1", 5432);
			System.out.println("Succeed connect to server");

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			id = gui.id;
			out.writeUTF(id);
			System.out.println("reconnect - writeUTF(id)");

			while(in != null) {
				msg = in.readUTF();
				gui.appendMsg(msg);
			}
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
}