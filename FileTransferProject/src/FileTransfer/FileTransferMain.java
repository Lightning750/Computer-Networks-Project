package FileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTransferMain {

	private static Network.Protocol protocol;
	private static ByteBuffer byteBuffer;

	public static void main(String[] args) throws IOException {
		//Displays IP Address
		InetAddress inetAddress = InetAddress.getLocalHost();
		System.out.println("IP Address:- " + inetAddress.getHostAddress());
		System.out.println("Host Name:- " + inetAddress.getHostName());

		Scanner input = new Scanner(System.in);
		int userPrompt = 0;
		while (userPrompt == 0){
			System.out.println("Which protocol would you like to use.");
			System.out.print("Enter 1 for UDP or 2 for TCP: ");
			userPrompt = input.nextInt();
			if (userPrompt == 1){
				System.out.println("You have selected UDP" );
				protocol = Network.Protocol.UDP;
			}
			else if (userPrompt == 2){
				if (userPrompt == 1){
					System.out.println("You have selected UDP" );
					protocol = Network.Protocol.UDP;
				}
				else if (userPrompt == 2){
					System.out.println("You have selected TCP");
					protocol = Network.Protocol.TCP;
				}
				else{
					System.out.println("You have not selected a valid response" );
					userPrompt = 0;
				}
			}
		}
		
		userPrompt = 0;
		while (userPrompt == 0){
			System.out.println("Would you like to be the client or server?");
			System.out.print("Enter 1 for Client or 2 for Server: ");
			userPrompt = input.nextInt();
			if (userPrompt == 1){
				System.out.println("You have selected Client" );
				client();
			}
			else if (userPrompt == 2){
				System.out.println("You have selected Server");
				server();
			}
			else{
				System.out.println("You have not selected a valid response" );
				userPrompt = 0;
			}
		}
		input.close();
	}

	public static void server() throws IOException {
		FileTransferServer server;
		switch(protocol) {
		case UDP:
			server = new FileTransferServer(protocol, Network.UDP_PORT);
			break;
		case TCP:
		default:
			server = new FileTransferServer(protocol, Network.TCP_PORT);
			break;
		}
		
		System.out.print("Enter any character if you are ready to start the connection: ");
		Scanner scanner = new Scanner(System.in);
		while(!scanner.hasNext());
		System.out.println("Connecting...");
		server.acceptConnection();
		System.out.println("Connection confirmed");
		
		String fileName = server.receiveString();
		File input = new File(fileName);
		if(input.exists()){
			//send ACK
			server.sendInt(1);
			String fileExists = "The file requested has been found";
			System.out.println(fileExists);
			server.sendString(fileExists);
			
			//wait for ACK from client
			server.receiveInt();
			
			//calculate and send checksum
			String checkSum = getFileChecksum(input);
			server.sendString(checkSum);
			
			//setup file transfer
			FileInputStream fis = new FileInputStream(input);
			byte[] byteArray = new byte[2048];
			int bytesCount = 0;
			int packetNum = 0;

			//send expected number of packets
			int packets = (int) Math.ceil((double)fis.available()/1024);
			server.sendInt(packets);
			
			//send file 1024 bytes at a time
			while ((bytesCount = fis.read(byteArray, 4, 1024)) != -1) {
				byteBuffer = ByteBuffer.allocate(2048);
				byteBuffer.putInt(packetNum);
				byteBuffer.put(byteArray, 0, bytesCount);
				byteBuffer.rewind();
				byteBuffer.get(byteArray, 0, bytesCount + 4);
				server.sendBytes(byteArray, bytesCount + 4);
				packetNum++;
			}
			System.out.println("File sent");
			server.receiveInt();
			server.sendInt(1);
			fis.close();
			scanner.close();
		}
		else{
			server.sendInt(0);
			server.sendString("The file requested was not found");
		}
	}

	public static void client() throws IOException {
		FileTransferClient client;
		switch(protocol) {
		case UDP:
			client = new FileTransferClient(protocol, Network.UDP_PORT);
			break;
		case TCP:
		default:
			client = new FileTransferClient(protocol, Network.TCP_PORT);
		}
		System.out.print("Enter the IP Address you would like to connect to: ");
		Scanner scanner = new Scanner(System.in);
		String IPAddress = scanner.nextLine();
		InetAddress serverAddress = InetAddress.getByName(IPAddress);
		System.out.print("Connecting...");
		client.beginConnection(serverAddress);	
		System.out.println("Connection confirmed");
		
		//Prompts user to enter file name.
		System.out.print("Enter file name: ");
		String File = scanner.nextLine();
		client.sendString(File);
		
		int ack = client.receiveInt();
		String message = client.receiveString();
		System.out.println(message);
		if (ack ==1)
		{
			//send confirmation back to server
			client.sendInt(ack);
			//receive checksum
			String CheckSum = client.receiveString();
		
			int packetNum = client.receiveInt();
			ArrayList<byte[]> fileBuffer = new ArrayList<byte[]>(packetNum);
			byte[] packet = new byte[1024];
			
			for (int i=0; i<packetNum; i++)
			{
				byteBuffer = ByteBuffer.wrap(client.receiveBytes());
				int index = byteBuffer.getInt();
				byteBuffer.get(packet, 0, byteBuffer.remaining());
				fileBuffer.add(index, packet);				
			}
			
			FileOutputStream fos = new FileOutputStream(File);
			for (int i=0; i<packetNum; i++){
				fos.write(fileBuffer.get(i));
			}
			System.out.println("Recieved file");
			client.sendInt(1);
			client.receiveInt();
			fos.close();
			scanner.close();
		}
		else
		{
			client.receiveString();
			
		}
	}
	
	private static String getFileChecksum(File file) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		//Read file data and update in message digest
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}
		fis.close();
		

		//Get the md5 checksum
		byte[] bytes = digest.digest();

		//This bytes[] has bytes in decimal format;
		//Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
