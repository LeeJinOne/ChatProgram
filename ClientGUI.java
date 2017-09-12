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

public class TClientGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea(40, 25);
	private JTextArea idArea = new JTextArea(1, 20); // ID값이 표시되는 곳
	private JTextField textField = new JTextField(25);
	private JScrollPane scrollPane = new JScrollPane(textArea);
	private Font font = new Font("돋움", Font.PLAIN, 20);

	private TClientBackground tcb = new TClientBackground();
	private String msg;
	String id;

	public TClientGUI() {
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

		tcb.setGui(this);
		tcb.connect();
	}

	public static void main(String[] args) {
		new TClientGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String msg = textField.getText();
		if(msg.equalsIgnoreCase("-exit")) { 
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("userID", id);
			jsonObject.put("body", msg);

			String message = jsonObject.toJSONString();

			tcb.sendMessage(message);
			try{
				tcb.out.flush();
				tcb.in.close();
				tcb.out.close();
				tcb.socket.close();

			} catch (IOException ie) {
				ie.printStackTrace();
			}
		  	System.out.println("Socket Connected : " + tcb.socket.isConnected());
			System.out.println("Socket Closed : " + tcb.socket.isClosed() + "\n");
			System.exit(0);
		} else {
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("userID", id);
			jsonObject.put("body", msg);

			String message = jsonObject.toJSONString();

			tcb.sendMessage(message);
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