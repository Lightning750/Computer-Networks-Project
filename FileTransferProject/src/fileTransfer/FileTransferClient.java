package fileTransfer;

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

//The FileTransferClient class handles sending and receiving data for either
//TCP or UDP. All methods support either TCP or UDP based on the protocol
//chosen upon construction. FileTransferClient initiates a connection with
//a corresponding FileTransferServer.
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
	
	//constructs client and initializes a socket for the selected protocol
	//and port
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
	
	//begins a connection with a computer running FileTransferServer
	public void beginConnection(InetAddress IP) throws IOException {
		serverIP = IP;
		
		switch(protocol) {
		case UDP:
			udpSocket.setSoTimeout(Network.TIMEOUT);
			udpSocket.connect(serverIP, port);
			
			//used for setting up connection, contents not important
			byte[] beginMessage = {0, 1, 2, 3, 4, 5, 6, 7};
			DatagramPacket beginPacket = 
					new DatagramPacket(beginMessage, 0, 8, serverIP, port);
			
			//mock TCP handshake
			udpSocket.send(beginPacket);
			udpSocket.receive(beginPacket);
			udpSocket.send(beginPacket);
			break;
			
		case TCP:
		default:
			tcpSocket = new Socket(serverIP, port, InetAddress.getLocalHost(), port);
			tcpSocket.setSoTimeout(Network.TIMEOUT);
			//tcpSocket.connect(new InetSocketAddress(serverIP, port), Network.TIMEOUT);
			writeBuffer = new DataOutputStream(tcpSocket.getOutputStream());
			readBuffer = new DataInputStream(tcpSocket.getInputStream());
			break;
		}
	}
	
	public void sendInt(int data) throws IOException {
		switch(protocol) {
		case UDP:
			//package int with an identifier byte
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
	
	public void sendBytes(byte[] byteArray, int offset, int length) throws IOException {
		switch(protocol) {
		case UDP:
			//package length bytes with an additional identifier byte
			byteBuffer = ByteBuffer.allocate(length + 1);
			byteBuffer.put(Network.byteID);
			byteBuffer.put(byteArray, offset, length);
			
			DatagramPacket bytePacket =
				new DatagramPacket(byteBuffer.array(), 0, length + 1, serverIP, port);
			udpSocket.send(bytePacket);
			break;
			
		case TCP:
		default:
			//sendBytes() and receiveBytes() internally communicate the number
			//of bytes sent. This value is not returned to the caller
			writeBuffer.writeInt(length);
			
			//data returned by reveiveBytes
			writeBuffer.write(byteArray, offset, length);
			break;
		}
	}
	
	public void sendString(String message)throws IOException {
		switch(protocol) {
		case UDP:
			byte[] byteArray = message.getBytes();
			int length = byteArray.length;
			
			//package string with an identifier byte
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
		int data;
		switch(protocol) {
		case UDP:
			//receive int
			byte[] byteArray = udpReceiveType(Network.intID);
			
			byteBuffer = ByteBuffer.wrap(byteArray, 0, 5);
			byteBuffer.get(); //throw away identifier byte
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
			//receive byte[]
			byteArray = udpReceiveType(Network.byteID);
			
			byteBuffer = ByteBuffer.wrap(byteArray, 0, byteArray.length);
			byteBuffer.get(); //throw away identifier byte
			byteArray = new byte[byteArray.length - 1];
			byteBuffer.get(byteArray, 0, byteArray.length);
			return byteArray;
			
		case TCP:
		default:
			//receive length of byte array from sendBytes()
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
			//receive string
			byte[] stringArray = udpReceiveType(Network.stringID);
			
			//offset argument of 1 throws away identifier byte
			string = new String(stringArray, 1, stringArray.length - 1);
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
	
	//UDP only private method that ensures the datagram received is of the
	//requested type. Stores out of order packets and returns them when a
	//datagram of the corresponding type is requested. Does not account 
	//for out of order packets of the same type.
	private byte[] udpReceiveType(byte type) throws IOException {
		//check for stored datagram of the correct type
		for(int i = 0; i < datagramStorage.size(); i++) {
			if (datagramStorage.get(i)[0] == type) {
				byte[] data = datagramStorage.get(i);
				datagramStorage.remove(i);
				return data;
			}
		}
		
		//try up to 5 times to receive a packet of the correct type
		for(int i = 0; i < 5; i++) {
			//max packet size is:
			//Network.PACKET_SIZE + 4 (int packetNum) + 1 (byte identifier)
			int maxSize = Network.PACKET_SIZE + 5;
			byte[] buffer = new byte[maxSize];
			DatagramPacket newPacket = 
					new DatagramPacket(buffer, maxSize);
			udpSocket.receive(newPacket);
			
			//get length of data received
			byte[] data = new byte[newPacket.getLength()];
			//buffer is still a byte[maxSize]
			buffer = newPacket.getData();
			//truncate extra bytes not filled with data
			for(int j=0; j<data.length; j++)
				data[j] = buffer[j];
			
			if (data[0] == type) return data;
			else datagramStorage.add(data);
		}
		//if expected packet type not received after 5 tries
		throw new IOException("Expected packet not received");
	}
}