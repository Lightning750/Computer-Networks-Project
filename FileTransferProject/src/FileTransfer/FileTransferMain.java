package FileTransfer;

import java.io.IOException;
import java.util.Scanner;
import java.net.InetAddress;

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
		System.out.print("Enter any key if you are ready to start the connection: ");
		Scanner connection = new Scanner(System.in);
		while(!connection.hasNext());
		System.out.println("Connecting...");
		server.acceptConnection();
		System.out.println("Connection confirmed");
		server.receiveString();
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
		Scanner FileName = new Scanner(System.in);
		String File = FileName.nextLine();
		client.sendString(File);
		int ack = client.receiveInt();
		if (ack ==1)
		{
			int CheckSum = client.receiveInt();
		}
		else
		{
			// todo
		}
	}
}
