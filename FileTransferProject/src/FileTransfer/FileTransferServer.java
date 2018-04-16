package FileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer {
	private Network.Protocol protocol; 
	private DatagramSocket UDP_Socket;
	private ServerSocket TCP_ServerSocket;
	private Socket TCP_Socket;
	private int port;
	private InetAddress clientIP;
	private DataOutputStream writeBuffer;
	private DataInputStream readBuffer;

	FileTransferServer(Network.Protocol p, int port) throws IOException {
		protocol = p;
		this.port = port;
		switch(protocol) {
		case UDP:
			UDP_Socket = new DatagramSocket(port);
			break;
		case TCP:
		default:
			TCP_ServerSocket = new ServerSocket(port);
			break;			
		}
	}
	
	public void acceptConnection() throws IOException {
		switch(protocol) {
		case UDP:
			UDP_Socket.setSoTimeout(Network.TIMEOUT);
			byte[] connectionBuffer = null;
			DatagramPacket connectionPacket = 
					new DatagramPacket(connectionBuffer, 8);
			UDP_Socket.receive(connectionPacket);
			UDP_Socket.send(connectionPacket);
			UDP_Socket.receive(connectionPacket);
			clientIP = connectionPacket.getAddress();
			break;
		case TCP:
		default:
			TCP_ServerSocket.setSoTimeout(Network.TIMEOUT);
			TCP_Socket = TCP_ServerSocket.accept();
			clientIP = TCP_Socket.getInetAddress();
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
	
	public byte[] receiveBytes(int length) throws IOException {
		byte[] byteArray = { 0 };
		switch(protocol) {
		case UDP:
			return byteArray;
		case TCP:
		default:
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

