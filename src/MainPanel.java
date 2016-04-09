import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class MainPanel extends JPanel {
	private JLabel inetLabel;
	private SpringLayout layout;
	private JButton sendButton;
	private JButton portButton;
	private JButton clearConsoleButton;
	private int port;
	private JTextArea textArea;
	private JScrollPane textPane;
	private DatagramSocket receiveSocket;
	private JProgressBar progress;

	private ActionListener clearConsoleListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			textArea.setText("");
		}
	};
	
	private ActionListener sendListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String address = JOptionPane.showInputDialog(null, "Enter Computer Name", "Name", JOptionPane.INFORMATION_MESSAGE);
			try {
				InetAddress sendAddress = InetAddress.getByName(address);
				JFileChooser choose = new JFileChooser();
				choose.showOpenDialog(null);
				File f = choose.getSelectedFile();
				if (f == null) return;
				String port = JOptionPane.showInputDialog(null, "Enter Port to Send to", "Send Port", JOptionPane.INFORMATION_MESSAGE);
				int sendPort = Integer.parseInt(port);
				SendFile sending = new SendFile(sendAddress, f, sendPort);
				sending.send(progress, textArea);
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(null, "Unable To Connect!", "Connection", JOptionPane.ERROR_MESSAGE);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Invalid Number!", "Invalid", JOptionPane.ERROR_MESSAGE);
			}


		}
	};

	private ActionListener portListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String portString = JOptionPane.showInputDialog(null, "Enter Port Number", "Port", JOptionPane.INFORMATION_MESSAGE);
			int tempPort = 0;
			try {
				tempPort = Integer.parseInt(portString);
				DatagramSocket test = new DatagramSocket(tempPort);
				test.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Invalid Port!", "Invalid!", JOptionPane.ERROR_MESSAGE);
			}
			if (tempPort != 0) {
				port = tempPort;
			}
		}
	};

	public MainPanel(int initialPort) {
		layout = new SpringLayout();
		sendButton = new JButton("SEND!");
		portButton = new JButton("Set Port");
		clearConsoleButton = new JButton("Clear!");
		layout.putConstraint(SpringLayout.NORTH, clearConsoleButton, 25, SpringLayout.SOUTH, portButton);
		layout.putConstraint(SpringLayout.WEST, clearConsoleButton, 0, SpringLayout.WEST, sendButton);
		progress = new JProgressBar(0, 100);
		textArea = new JTextArea();
		textPane = new JScrollPane(textArea);
		layout.putConstraint(SpringLayout.WEST, progress, 121, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, progress, -21, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, progress, -22, SpringLayout.EAST, this);
		progress.setValue(50);
		layout.putConstraint(SpringLayout.NORTH, portButton, 23, SpringLayout.SOUTH, sendButton);
		layout.putConstraint(SpringLayout.WEST, portButton, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, sendButton, 10, SpringLayout.WEST, this);
		try {
			receiveSocket = new DatagramSocket(initialPort);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable To Bind!", "Connection", JOptionPane.ERROR_MESSAGE);
		}
		setupPanel();
		receive();
	}

	public void receive() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] b = new byte[61140];
				DatagramPacket initPacket = new DatagramPacket(b, 61140);
				try {
					receiveSocket.receive(initPacket);
					System.out.println("Received a Packet");
				} catch (IOException e) {
					e.printStackTrace();
				}
				ReceiveFile receive = new ReceiveFile(initPacket, textArea, progress);
			}
		}).start();
	}

	public void setupPanel() {
		this.setLayout(layout);
		sendButton.addActionListener(sendListener);
		portButton.addActionListener(portListener);
		this.add(sendButton);
		this.add(portButton);
		this.add(progress);
		this.add(clearConsoleButton);
		clearConsoleButton.addActionListener(clearConsoleListener);
		try {
			inetLabel = new JLabel("You are connected to: " + InetAddress.getLocalHost().getHostName());
			inetLabel.setFont(new Font(inetLabel.getName(), Font.BOLD, 15));
			layout.putConstraint(SpringLayout.NORTH, sendButton, 53, SpringLayout.SOUTH, inetLabel);
			this.add(inetLabel);
			layout.putConstraint(SpringLayout.NORTH, textPane, 24, SpringLayout.SOUTH, inetLabel);
			layout.putConstraint(SpringLayout.WEST, textPane, 0, SpringLayout.WEST, progress);
			layout.putConstraint(SpringLayout.SOUTH, textPane, 293, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, textPane, 0, SpringLayout.EAST, progress);
			add(textPane);
			textArea.setEditable(false);
		} catch (UnknownHostException e) {
			System.out.println("Cannot connect to Internet");
		}
	}
}
