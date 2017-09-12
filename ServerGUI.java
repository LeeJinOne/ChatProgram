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

public class TServerGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea(40,25);
	private JTextArea idArea = new JTextArea(1, 20); // ID값이 표시되는 곳
	public JTextArea listArea = new JTextArea(10, 10);
	private JTextField textField = new JTextField(25);
	private JScrollPane scrollPane = new JScrollPane(textArea);
	private Font font = new Font("돋움", Font.PLAIN, 20);

	private TServerBackground tsb = new TServerBackground();

	private String msg;
	private String type;


	private final String serverID = "Server";

	public TServerGUI() throws IOException {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setSize(600,600);
		setTitle("Server Part");

		// textArea.setEditable(false); // JTextArea 편집이 불가능하도록 설정
		add(idArea, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(listArea, BorderLayout.EAST);
		add(textField, BorderLayout.SOUTH);
		textField.addActionListener(this);

		idArea.setFont(font);
		textArea.setFont(font);
		textField.setFont(font);

		idArea.append(serverID);
		tsb.setGui(this);
		tsb.setting();
	}

	public static void main(String[] args) throws IOException {
		new TServerGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String msg = textField.getText();
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("userID", serverID);
		jsonObject.put("body", msg);

		String message = jsonObject.toJSONString();

		tsb.receiveMessage(message);
		textField.setText("");
	}

	public void appendMsg(String msg) {
		textArea.append(msg + "\n");
	}
}