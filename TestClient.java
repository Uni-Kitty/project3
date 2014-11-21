import java.io.*;
import java.net.*;
import java.nio.*;

/**
 * Project 3 test client
 */
public class TestClient {

    private static final int serverPort = 9999;
    private static final int clientPort = 9998;
    private static final String URL = "54.69.151.4";
    
    public static void main(String arg[]) throws Exception {
        DatagramSocket socket = new DatagramSocket(9998);
        InetAddress address = InetAddress.getByName(URL);
        String message = "Secret message omg!";
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPort);
        socket.send(packet);
        byte[] receivedData = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length, address, clientPort);
        socket.receive(receivedPacket);
        System.out.println("Received packet:");
        System.out.println(new String(receivedData));
    }
}