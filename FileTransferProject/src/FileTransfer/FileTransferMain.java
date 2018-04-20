package FileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTransferMain {

	private static Network.Protocol protocol;

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
				return;
			}
			else if (userPrompt == 2){
				System.out.println("You have selected Server");
				server();
				return;
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
		Scanner connection = new Scanner(System.in);
		while(!connection.hasNext());
		System.out.println("Connecting...");
		server.acceptConnection();
		System.out.println("Connection confirmed");
		
		String fileName = server.receiveString();
		File input = new File(fileName);
		if(!input.exists()){
			System.out.println("The file requested has been found");
			String fileExists = "The file requested has been found";
			server.sendString(fileExists);
			//This is where file will split
			FileInputStream fis = new FileInputStream(input);
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;

			int packets = (int) Math.ceil(fis.available()/1024);
			server.sendInt(packets);

			while ((bytesCount = fis.read(byteArray)) != -1) {
				server.sendBytes(byteArray);
			}
			fis.close();
		}
		else{
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
		Scanner IPConnect = new Scanner(System.in);
		String IPAddress = IPConnect.nextLine();
		InetAddress serverAddress = InetAddress.getByName(IPAddress);
		client.beginConnection(serverAddress);	
		System.out.println("Connection confirmed");
		
		//Prompts user to enter file name. 
		Scanner FileName = new Scanner(System.in);
		System.out.print("Enter file name");
		String File = FileName.nextLine();
		client.sendString(File);
		int ack = client.receiveInt();
		if (ack ==1)
		{
			int CheckSum = client.receiveInt();
		}
		else
		{
			//TODO
			
		}
		int fileLength = client.receiveInt();
		int lastPacket = fileLength % Network.PACKET_SIZE;
		for (int i=0; i<fileLength; i+=1000)
		{
			client.receiveBytes();
		}
		client.receiveBytes();
	}
	
	private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
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
