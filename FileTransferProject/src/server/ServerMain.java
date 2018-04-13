package server;

import java.io.IOException;

import com.esotericsoftware.kryonet.*;

public class ServerMain {

	public static void main(String[] args) {
		Server server = new Server();
	    server.start();
	    try {
	    server.bind(54555, 54777);
	    } catch(IOException e) {
	    	
	    }
	}

}
