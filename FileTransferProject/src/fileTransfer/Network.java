package fileTransfer;

//This class holds global constants used by other fileTransfer classes
public class Network {
	public static final int TCP_PORT = 55551;
	public static final int UDP_PORT = 55552;
	public static final int TIMEOUT = 15000; //15 seconds
	public static final int PACKET_SIZE = 1024; //for byte[] packets
	public static enum Protocol { TCP, UDP };
	
	//used for identifying UDP packet types
	public static final byte intID = 1;
	public static final byte byteID = 2;
	public static final byte stringID = 4;
	
}
