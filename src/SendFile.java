import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.swing.JProgressBar;

public class SendFile {
	private InetAddress sendAddress;
	private File sendFile;
	private int sendPort;
	private DatagramSocket sendSocket;
	private int sendingAmount = 20480;

	public SendFile(InetAddress sendAddress, File sendFile, int sendPort) {
		this.sendAddress = sendAddress;
		this.sendFile = sendFile;
		this.sendPort = sendPort;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method creates the request that the server sends.
	 *
	 * @param requestType
	 *            The type of request being set (Write, Read, Invalid). Default
	 *            is Read
	 * @param fileName
	 *            the name of the file
	 * @param mode
	 *            the mode being used
	 * @return the byte array that of the request
	 */
	public byte[] createRequest() {
		byte[] b = sendFile.getName().getBytes();
		return b;
	}

	public void send() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Sending File: " + sendFile);
				System.out.println("To Address: " + sendAddress);
				System.out.println("And Port: " + sendPort);
				JProgressBar bar = new JProgressBar(0, 100);
				byte[] b = createRequest();
				DatagramPacket sendPacket = new DatagramPacket(b, b.length, sendAddress, sendPort);
				try {
					sendSocket.send(sendPacket);
					sendSocket.receive(sendPacket);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				int newSendPort = sendPacket.getPort();
				try {
					BufferedInputStream input = new BufferedInputStream(new FileInputStream(sendFile));
					byte[] sendingData = new byte[sendingAmount];
					int totalSent = 0;
					int x;
					System.out.println("Send Socket: " + sendSocket.getLocalPort());
					while ((x = input.read(sendingData)) != -1) {
						totalSent += x;
						long avg = totalSent/sendFile.length();
						int averg = Math.toIntExact(avg);
						bar.setValue(averg);
						sendPacket = new DatagramPacket(sendingData, sendingData.length, sendAddress, newSendPort);
						sendSocket.send(sendPacket);
						System.out.println("Attempting to receive ACK");
						sendSocket.setSoTimeout(1500);
						try {
							sendSocket.receive(sendPacket);	
						} catch (SocketTimeoutException e) {
							sendSocket.setSoTimeout(0);
							System.out.println("Timed Out! Resending");
							sendSocket.send(sendPacket);
							sendSocket.receive(sendPacket);
						}
						System.out.println("Received a Packet!");
					}
					byte[] empty = {};
					DatagramPacket sendPacket1 = new DatagramPacket(empty, empty.length, sendAddress, newSendPort);
					sendSocket.send(sendPacket1);
					System.out.println("Finished Sending!");
				} catch (Exception e) {}
			}
		}).start();
	}
}
