package test.client;

import java.awt.Font;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import org.json.simple.JSONObject;

public class ClientGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea(40, 25);
	private JTextArea idArea = new JTextArea(1, 20); // ID값이 표시되는 곳
	private JTextField textField = new JTextField(25);
	private JScrollPane scrollPane = new JScrollPane(textArea);
	private Font font = new Font("Serif", Font.PLAIN, 20);

	private ClientBackground cb = new ClientBackground();
	private String msg;
	String id;

	public ClientGUI() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setSize(600,600);
		setTitle("Client Part");

		add(idArea, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(textField, BorderLayout.SOUTH);
		textField.addActionListener(this);

		idArea.setFont(font);
		textArea.setFont(font);
		textField.setFont(font);

		cb.setGui(this);
		cb.connect();
	}

	public static void main(String[] args) {
		new ClientGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String msg = textField.getText();
		if(msg.equalsIgnoreCase("-exit")) { 
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("userID", id);
			jsonObject.put("body", msg);

			String message = jsonObject.toJSONString();

			cb.sendMessage(message);
			try{
				cb.out.flush();
				cb.in.close();
				cb.out.close();
				cb.socket.close();
				/* Do I change code in 'try'? */
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		  	System.out.println("Socket Connected : " + cb.socket.isConnected());
			System.out.println("Socket Closed : " + cb.socket.isClosed() + "\n");
			// System.exit(0); // <=== **Delete this code**
		/* ================================================= */
		/* These are where I want to recode and do something */
		} else if (msg.equalsIgnoreCase("-reconnect")) {
			ClientBackground cb = new ClientBackground();
			cb.reconnect();
		/* ================================================= */
		/* ================================================= */
		} else {
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("userID", id);
			jsonObject.put("body", msg);

			String message = jsonObject.toJSONString();

			cb.sendMessage(message);
		}
		textField.setText("");
	}

	public void appendMsg(String msg) {
		textArea.append(msg + "\n");
		textArea.setCaretPosition(textArea.getDocument().getLength()); // 맨 아래로 스크롤
	}

	public void getID(String id) {
		this.id = id;
		idArea.append(id);
	}
}