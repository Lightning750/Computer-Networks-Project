package FileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FileTransferClient {
	private Network.Protocol protocol; 
	private DatagramSocket udpSocket;
	private Socket tcpSocket;
	private int port;
	private InetAddress serverIP;
	private DataOutputStream writeBuffer;
	private DataInputStream readBuffer;
	private ByteBuffer byteBuffer;
	private ArrayList<byte[]> datagramStorage;
	
	FileTransferClient(Network.Protocol p, int port) throws IOException {
		protocol = p;
		this.port = port;
		datagramStorage = new ArrayList<byte[]>();
		switch(protocol) {
		case UDP:
			udpSocket = new DatagramSocket(port);
			break;
		case TCP:
		default:
			tcpSocket = new Socket();
			break;		
		}
	}
	
	public void beginConnection(InetAddress IP) throws IOException {
		serverIP = IP;
		switch(protocol) {
		case UDP:
			udpSocket.setSoTimeout(Network.TIMEOUT);
			udpSocket.connect(serverIP, port);
			byte[] beginMessage = {0, 1, 2, 3, 4, 5, 6, 7};
			DatagramPacket beginPacket = 
					new DatagramPacket(beginMessage, 0, 8, serverIP, port);
			udpSocket.send(beginPacket);
			udpSocket.receive(beginPacket);
			udpSocket.send(beginPacket);
			break;
		case TCP:
		default:
			tcpSocket.setSoTimeout(Network.TIMEOUT);
			tcpSocket.connect(new InetSocketAddress(serverIP, port), Network.TIMEOUT);
			writeBuffer = new DataOutputStream(tcpSocket.getOutputStream());
			readBuffer = new DataInputStream(tcpSocket.getInputStream());
			break;
		}
	}
	
	public void sendInt(int data) throws IOException {
		switch(protocol) {
		case UDP:
			byteBuffer = ByteBuffer.allocate(5);
			byteBuffer.put(Network.intID);
			byteBuffer.putInt(data);
			DatagramPacket intPacket = 
				new DatagramPacket(byteBuffer.array(), 5, serverIP, port);
			udpSocket.send(intPacket);
			break;
		case TCP:
		default:
			writeBuffer.writeInt(data);
			break;
		}
	}
	
	public void sendBytes(byte[] byteArray, int length) throws IOException {
		switch(protocol) {
		case UDP:
			byteBuffer = ByteBuffer.allocate(length + 1);
			byteBuffer.put(Network.byteID);
			byteBuffer.put(byteArray, 0, length);
			DatagramPacket bytePacket =
				new DatagramPacket(byteBuffer.array(), 0, length + 1, serverIP, port);
			udpSocket.send(bytePacket);
			break;
		case TCP:
		default:
			writeBuffer.writeInt(length);
			writeBuffer.write(byteArray, 0, length);
			break;
		}
	}
	
	public void sendString(String message)throws IOException {
		switch(protocol) {
		case UDP:
			byte[] byteArray = message.getBytes();
			int length = byteArray.length;
			byteBuffer = ByteBuffer.allocate(length + 1);
			byteBuffer.put(Network.stringID);
			byteBuffer.put(byteArray, 0, length);
			DatagramPacket stringPacket =
				new DatagramPacket(byteBuffer.array(), length + 1, serverIP, port);
			udpSocket.send(stringPacket);
			break;
		case TCP:
		default:
			writeBuffer.writeUTF(message);
			break;			
		}
	}
	
	public int receiveInt() throws IOException {
		int data = 0;
		switch(protocol) {
		case UDP:
			byte[] byteArray = udpReceiveType(Network.intID);
			byteBuffer = ByteBuffer.wrap(byteArray, 0, 5);
			byteBuffer.get();
			data = byteBuffer.getInt();
			return data;
		case TCP:
		default:
			data = readBuffer.readInt();
			return data;	
		}
	}
	
	public byte[] receiveBytes() throws IOException {
		byte[] byteArray;
		switch(protocol) {
		case UDP:
			byteArray = udpReceiveType(Network.byteID);
			byteBuffer = ByteBuffer.wrap(byteArray, 0, byteArray.length);
			byteBuffer.get();
			byteArray = new byte[byteArray.length - 1];
			byteBuffer.get(byteArray, 0, byteArray.length);
			return byteArray;
		case TCP:
		default:
			int length = receiveInt();
			byteArray = new byte[length];
			readBuffer.readFully(byteArray, 0, length);
			return byteArray;
		}
	}
	
	public String receiveString() throws IOException {
		String string;
		switch(protocol) {
		case UDP:
			byte[] byteArray = udpReceiveType(Network.stringID);
			string = new String(byteArray, 1, byteArray.length - 1);
			return string;
		case TCP:
		default:
			string = readBuffer.readUTF();
			return string;		
		}
	}
	
	public void closeConnection() throws IOException {
		switch(protocol) {
		case UDP:
			udpSocket.close();
			break;
		case TCP:
		default:
			tcpSocket.close();
			break;
		}
	}
	
	private byte[] udpReceiveType(byte t) throws IOException {
		for(int i = 0; i < datagramStorage.size(); i++) {
			if (datagramStorage.get(i)[0] == t) {
				byte[] b = datagramStorage.get(i);
				datagramStorage.remove(i);
				return b;
			}
		}
		for(int i = 0; i < 5; i++) {
			DatagramPacket newPacket = 
					new DatagramPacket(new byte[2048], 2048);
			udpSocket.receive(newPacket);
			byte[] b = new byte[newPacket.getLength()];
			byte[] data = newPacket.getData();
			for(int j=0; j<b.length; j++)
				b[j] = data[j];
			if (b[0] == t) return b;
			else datagramStorage.add(b);
		}
		throw new IOException("Expected packet not received");
	}
}