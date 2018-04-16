package FileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileTransferClient {
	private Network.Protocol protocol; 
	private DatagramSocket UDP_Socket;
	private Socket TCP_Socket;
	private int port;
	private InetAddress serverIP;
	private DataOutputStream writeBuffer;
	private DataInputStream readBuffer;
	
	FileTransferClient(Network.Protocol p, int port) throws IOException {
		protocol = p;
		this.port = port;
		switch(protocol) {
		case UDP:
			UDP_Socket = new DatagramSocket(port);
			break;
		case TCP:
		default:
			TCP_Socket = new Socket();
			break;		
		}
	}
	
	public void beginConnection(InetAddress IP) throws IOException {
		serverIP = IP;
		switch(protocol) {
		case UDP:
			UDP_Socket.setSoTimeout(Network.TIMEOUT);
			byte[] beginMessage = {0, 1, 2, 3, 4, 5, 6, 7};
			DatagramPacket beginPacket = 
					new DatagramPacket(beginMessage, 0, 8, serverIP, port);
			UDP_Socket.send(beginPacket);
			UDP_Socket.receive(beginPacket);
			UDP_Socket.send(beginPacket);
			break;
		case TCP:
		default:
			TCP_Socket.setSoTimeout(Network.TIMEOUT);
			TCP_Socket.connect(new InetSocketAddress(serverIP, port), Network.TIMEOUT);
			writeBuffer = new DataOutputStream(TCP_Socket.getOutputStream());
			readBuffer = new DataInputStream(TCP_Socket.getInputStream());
			break;
		}
	}
	
	public void sendBytes(byte[] byteArray) throws IOException {

	}
	public void sendString(String message)throws IOException {

	}
	public byte[] receiveBytes() throws IOException {

	}
	public String receiveString() throws IOException {

	}
}
