import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReceiveFile {
	private DatagramPacket packet;
	DatagramSocket socket;
	
	public ReceiveFile(DatagramPacket packet) {
		this.packet = packet;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
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
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
			while(true) {
				socket.send(acknowledge);
				socket.receive(packet);
				if (packet.getLength() == 0) break;
				out.write(packet.getData());
				System.out.println("Writing " + packet.getLength() + " bytes");
				System.out.println(packet.getLength());
			}
			out.close();
			System.out.print("Finished Receiving!");
		} catch (Exception e) {}
	}
	
}
