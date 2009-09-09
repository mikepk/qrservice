import java.io.*;
// PrintWriter;
// import java.io.InputStreamReader;
// import java.io.BufferedReader;
import java.net.*;
//import java.util.LinkedList;

public final class QRService {
    
    private QRService() {
    }
     
    public static void main(String[] args) throws Exception {
        System.out.println("Starting the QRService!");
        ServerSocket serverSocket = null;
        
        try {
            // serverSocket = new ServerSocket(9090,1,InetAddress.getLocalHost());
            serverSocket = new ServerSocket(9090,1,InetAddress.getByName("localhost"));

        } catch (IOException e) {
            System.out.println("Could not listen on port: 9090");
            System.exit(-1);
        }
        
        Socket clientSocket = null;
        
    while (true) {
        QRServiceWorker qw;        
        try {
            //server.accept returns a client connection
                  qw = new QRServiceWorker(serverSocket.accept());
                  Thread t = new Thread(qw);
                  t.start();


        } catch (IOException e) {
            System.out.println("Accept failed: 9090");
            System.exit(-1);
        }
        
        
        

        
    }//end while
        
        
//outside the while        
}// end main

}
