package FileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
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
			
		case TCP:
		default:
		}
	}
}
