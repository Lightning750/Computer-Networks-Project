package FileTransfer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.File;
import java.lang.System;
import java.net.*;

public class FileTransferServer {
	private PipedOutputStream writeBufferOut,readBufferOut;
	private PipedInputStream writeBufferIn,readBufferIn;
	private DatagramSocket UDP_Socket;
	private ServerSocket TCP_Socket;
	private Network.Protocol protocol; 
	
	public FileTransferServer(Network.Protocol p) throws IOException {
		this(16384, 2048, p);
	}

	FileTransferServer(int writeBufferSize, int readBufferSize, Network.Protocol p) throws IOException {
		writeBufferOut = new PipedOutputStream(writeBufferIn);
		writeBufferIn = new PipedInputStream(writeBufferOut, writeBufferSize);
		readBufferOut = new PipedOutputStream(readBufferIn);
		readBufferIn = new PipedInputStream(readBufferOut, readBufferSize);
		protocol = p;
	}
	
	public void bind(int port) throws IOException {
		switch(protocol) {
		case UDP:
			UDP_Socket = new DatagramSocket(port);
			break;
		case TCP:
		default:
			TCP_Socket = new ServerSocket(port);
			break;			
		}
	}
		
}

