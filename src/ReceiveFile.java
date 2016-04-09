import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class ReceiveFile {
	private DatagramPacket packet;
	private JTextArea textArea;
	private JProgressBar progress;
	DatagramSocket socket;

	public ReceiveFile(DatagramPacket packet, JTextArea textArea, JProgressBar progress) {
		this.packet = packet;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.textArea = textArea;
		this.progress = progress;
		receive();
	}

	public String extractFileName(byte[] b) {
		String receivedFileName = "";
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < b.length; index++) {
			if(b[index] != 0) {
				builder.append((char) b[index]);
			} else {
				receivedFileName = builder.toString();
				break;
			}
		}
		return receivedFileName;
	}

	public void receive() {
		String fileName = extractFileName(packet.getData());
		byte[] ack = {0,4};
		DatagramPacket acknowledge = new DatagramPacket(ack,2,packet.getAddress(),packet.getPort());
		try {
			int total = 0;
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
			textArea.append("----------------------------------\n");
			while(true) {
				socket.send(acknowledge);
				socket.setSoTimeout(3000);
				try {
					socket.receive(packet);
				} catch (SocketTimeoutException e) {
					System.out.println("TIEMOUT! RESENDING!");
					socket.send(acknowledge);
					try {
						socket.receive(packet);
					} catch (SocketTimeoutException e2) {
						socket.setSoTimeout(0);
						System.out.println("Timeout Again! Resending");
						socket.send(acknowledge);
						socket.receive(packet);
					}
				}
				if (packet.getLength() == 0) break;
				out.write(packet.getData());
				total+=packet.getLength();
				textArea.append("Writing " + packet.getLength() + " bytes\n");
				textArea.append("Total Printed: " + total + "\n");				
			}
			textArea.append("Finished Receiving!\n");
			textArea.append("----------------------------------\n");
			out.close();
		} catch (Exception e) {}
	}

}
