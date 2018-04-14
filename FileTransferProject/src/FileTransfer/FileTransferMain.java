package FileTransfer;

import java.io.IOException;
import java.util.Scanner;

public class FileTransferMain {
	
	private static Network.Protocol protocol;

	public static void main(String[] args) throws IOException {
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

				System.out.println("You have selected TCP");
				protocol = Network.Protocol.TCP;
			}
			else{
				System.out.println("You have not selected a valid response" );
				userPrompt = 0;
			}
		}
		userPrompt = 0;
		while (userPrompt == 0){
			System.out.println("Would you like to be the client or server?");
			System.out.print("Enter 1 for Client or 2 for Server: ");
			userPrompt = input.nextInt();
			if (userPrompt == 1){

				System.out.println("You have selected Client" );
			}
			else if (userPrompt == 2){

				System.out.println("You have selected Server");
			}
			else{
				System.out.println("You have not selected a valid response" );
				userPrompt = 0;
			}
		}
		
	}

}
