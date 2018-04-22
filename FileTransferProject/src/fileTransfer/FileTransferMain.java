package fileTransfer;

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
	private static Scanner input;
	
	public static void main(String[] args) throws IOException {
		//Displays IP Address and Host Name
		InetAddress inetAddress = InetAddress.getLocalHost();
		System.out.println("IP Address:- " + inetAddress.getHostAddress());
		System.out.println("Host Name:- " + inetAddress.getHostName());

		//prompt for protocol to use
		input = new Scanner(System.in);
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
					System.out.println("You have selected TCP");
					protocol = Network.Protocol.TCP;
			}
			else{
				System.out.println("You have not selected a valid response" );
				userPrompt = 0;
			}
		}
		
		//prompt for client or server
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
		while(!input.hasNext());
		System.out.println("Connecting...");
		server.acceptConnection();
		System.out.println("Connection confirmed");
		
		int continueReceiving = 1;
		while((continueReceiving = server.receiveInt()) != 0) {
			System.out.println("Client is selecting a file");

			String fileName = server.receiveString();
			File input = new File(fileName);
			if(input.exists()) {
				//send ACK
				server.sendInt(1);
				String fileExists = "The file requested has been found";
				System.out.println(fileExists);
				server.sendString(fileExists);

				//wait for ACK from client
				server.receiveInt();

				//calculate and send checksum
				String checksum = getFileChecksum(input);
				server.sendString(checksum);

				//setup file transfer
				FileInputStream fis = new FileInputStream(input);
				byte[] byteArray = new byte[Network.PACKET_SIZE + 4];
				int bytesCount, packetNum = 0;

				//send expected number of packets
				int packets = (int) Math.ceil((double)fis.available()/1024);
				server.sendInt(packets);

				//send file
				while ((bytesCount = fis.read(byteArray, 4, Network.PACKET_SIZE)) != -1) {
					byteBuffer = ByteBuffer.allocate(Network.PACKET_SIZE + 4);
					byteBuffer.putInt(packetNum);
					byteBuffer.put(byteArray, 4, bytesCount);
					byteBuffer.rewind();
					byteBuffer.get(byteArray, 0, bytesCount + 4);
					server.sendBytes(byteArray, 0, bytesCount + 4);
					packetNum++;
				}
				System.out.println("File sent");
				String checksumConfirmation = server.receiveString();
				System.out.println(checksumConfirmation);
				fis.close();
			}
			else{
				server.sendInt(0);
				String message = "The file requested was not found.";
				server.sendString(message);
				System.out.println(message);
			}
			server.receiveInt();
			server.sendInt(1);
		}
		System.out.println("Connection has been closed.");
		server.closeConnection();
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
		input = new Scanner(System.in);
		String IPAddress = input.nextLine();
		InetAddress serverAddress = InetAddress.getByName(IPAddress);
		System.out.println("Connecting...");
		client.beginConnection(serverAddress);	
		System.out.println("Connection confirmed");
		int continueSending = 1;
		
		while(continueSending == 1) {
			//Prompts user to enter file name or quit.
			System.out.print("Enter file  or \"exit\" to quit: ");
			String fileName = input.nextLine();
			if(fileName.equals("exit")) continueSending = 0;
			client.sendInt(continueSending);
			if(continueSending == 0) break;
			client.sendString(fileName);

			int ack = client.receiveInt();
			String message = client.receiveString();
			System.out.println(message);
			if (ack == 1)
			{
				//send confirmation back to server
				client.sendInt(ack);
				//receive checksum
				String checksum = client.receiveString();

				int packetNum = client.receiveInt(), index;
				ArrayList<byte[]> fileBuffer = new ArrayList<byte[]>(packetNum);
				byte[] packet, data;

				for (int i=0; i<packetNum; i++)
				{
					packet = client.receiveBytes();
					byteBuffer = ByteBuffer.wrap(packet);
					index = byteBuffer.getInt();
					data = new byte[byteBuffer.remaining()];
					byteBuffer.get(data, 0, byteBuffer.remaining());
					fileBuffer.add(index, data);			
				}
				File file = new File(fileName);
				FileOutputStream fos = new FileOutputStream(file);
				for(byte[] b : fileBuffer) fos.write(b);

				System.out.println("Received file");
				String newCheckSum = getFileChecksum(file);
				if(checksum.equals(newCheckSum))
				{
					message = "Checksums match. File recieved correctly.";
					System.out.println(message);
					client.sendString(message);
				}
				else
				{
					message = "Checksums don't match. File corrupted.";
					System.out.println(message);
					client.sendString(message);
				}
				fos.close();
			}
			else
			{
				System.out.println("Request a different file.");
			}
			client.sendInt(1);
			client.receiveInt();
		}
		System.out.println("Connection has been closed.");
		client.closeConnection();
	}
	
	
	//calculates checksum using MessageDigest and the MD5 algorithm
	private static String getFileChecksum(File file) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[Network.PACKET_SIZE];
		int bytesCount = 0;

		//Read file data and update message digest
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
