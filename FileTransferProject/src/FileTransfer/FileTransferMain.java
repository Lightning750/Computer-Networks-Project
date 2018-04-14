package FileTransfer;

import java.io.IOException;
import java.util.Scanner;

public class FileTransferMain {
	
	private static Network.Protocol protocol;

	public static void main(String[] args) throws IOException {
		

		
		Scanner input = new Scanner(System.in);
		int x = 0;
		int y = 0;
		
		while (x == 0){
			System.out.println("Which protocol would you like to use.");
			System.out.print("Enter 1 for UDP or 2 for TCP: ");


			x = input.nextInt();

			if (x == 1){

				System.out.println("You have selected UDP" );
				protocol = Network.Protocol.UDP;
			}
			else if (x == 2){

				System.out.println("You have selected TCP");
				protocol = Network.Protocol.TCP;
			}
			else{
				System.out.println("You have not selected a valid response" );
				x = 0;
			}
		}
		

		while (y == 0){
			System.out.println("Would you like to be the client or server?");
			System.out.print("Enter 1 for Client or 2 for Server: ");


			y = input.nextInt();

			if (y == 1){

				System.out.println("You have selected Client" );
			}
			else if (y == 2){

				System.out.println("You have selected Server");
			}
			else{
				System.out.println("You have not selected a valid response" );
				y = 0;
			}
		}
		
	}

}
