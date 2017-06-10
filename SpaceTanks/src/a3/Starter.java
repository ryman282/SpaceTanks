package a3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Starter {
	public static void main(String[] args) throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		System.out.println("Current IP Address: " + addr.getHostAddress());
		Scanner in = new Scanner(System.in);
		System.out.print("Enter Desired Server Address: ");
		String serverAddr = in.nextLine();
		System.out.println(serverAddr);
		System.out.print("Enter Desired Server Port Number: ");
		String serverPort = in.nextLine();
		System.out.println(serverPort);
		Game myGame = new Game(serverAddr, serverPort);
		myGame.start();
	}
}