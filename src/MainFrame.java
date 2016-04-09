import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	MainPanel mainPanel;

	public MainFrame(int initialPort) {
		super("File Transfer");
		mainPanel = new MainPanel(initialPort);
		this.setContentPane(mainPanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400,400);
		this.setVisible(true);
	}
	
	public static void main(String[] args) throws SocketException, UnknownHostException {
		int initialPort = 0;
		while (initialPort == 0) {
			String portString = JOptionPane.showInputDialog(null, "Enter Port To Receive On", "Port", JOptionPane.INFORMATION_MESSAGE);
			try {
				int port = Integer.parseInt(portString);
				if (port > 0) initialPort = port;
			} catch (NumberFormatException e) {}
		}
		MainFrame frame = new MainFrame(initialPort);
	}
	
}
