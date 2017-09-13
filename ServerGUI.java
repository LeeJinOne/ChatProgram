package test.server;

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

public class ServerGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea(40,25);
	private JTextArea idArea = new JTextArea(1, 20); // Place for mark ID.
	public JTextArea listArea = new JTextArea(10, 10);
	private JTextField textField = new JTextField(25);
	private JScrollPane scrollPane = new JScrollPane(textArea);
	private Font font = new Font("Serif", Font.PLAIN, 20);

	private ServerBackground sb = new ServerBackground();

	private String msg;
	private String type;


	private final String serverID = "Server";

	public ServerGUI() throws IOException {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setSize(600,600);
		setTitle("Server Part");

		add(idArea, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(listArea, BorderLayout.EAST);
		add(textField, BorderLayout.SOUTH);
		textField.addActionListener(this);

		idArea.setFont(font);
		textArea.setFont(font);
		textField.setFont(font);

		idArea.append(serverID);
		sb.setGui(this);
		sb.setting();
	}

	public static void main(String[] args) throws IOException {
		new ServerGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String msg = textField.getText();
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("userID", serverID);
		jsonObject.put("body", msg);

		String message = jsonObject.toJSONString();

		sb.receiveMessage(message);
		textField.setText("");
	}

	public void appendMsg(String msg) {
		textArea.append(msg + "\n");
	}
}