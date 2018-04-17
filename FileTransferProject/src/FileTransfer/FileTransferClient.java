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
	
	public void sendInt(int data) throws IOException {
		switch(protocol) {
		case UDP:
			
			break;
		case TCP:
		default:
			writeBuffer.writeInt(data);
			break;
		}
	}
	
	public void sendBytes(byte[] byteArray) throws IOException {
		switch(protocol) {
		case UDP:
			
			break;
		case TCP:
		default:
			writeBuffer.writeInt(byteArray.length);
			writeBuffer.write(byteArray);
			break;			
		}
	}
	
	public void sendString(String message)throws IOException {
		switch(protocol) {
		case UDP:
			
			break;
		case TCP:
		default:
			writeBuffer.writeChars(message);
			break;			
		}
	}
	
	public int receiveInt() throws IOException {
		int data = 0;
		switch(protocol) {
		case UDP:
			return data;
		case TCP:
		default:
			readBuffer.readInt();
			return data;	
		}
	}
	
	public byte[] receiveBytes() throws IOException {
		byte[] byteArray = { 0 };
		switch(protocol) {
		case UDP:
			return byteArray;
		case TCP:
		default:
			int length = receiveInt();
			readBuffer.readFully(byteArray, 0, length);
			return byteArray;		
		}
	}
	
	public String receiveString() throws IOException {
		String string = null;
		switch(protocol) {
		case UDP:
			return string;
		case TCP:
		default:
			string = readBuffer.readUTF();
			return string	;		
		}
	}
}
