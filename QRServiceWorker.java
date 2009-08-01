import java.io.*;
// PrintWriter;
// import java.io.InputStreamReader;
// import java.io.BufferedReader;
import java.net.*;
//import java.util.LinkedList;

public class QRServiceWorker implements Runnable {
    private Socket client;    
    
    public QRServiceWorker(Socket client) {
        this.client = client;
    }

    public void run() {
        //OutputStream raw_out = null;
        //InputStream raw_in = null;
        try {
            OutputStream raw_out = this.client.getOutputStream();

        // PrintWriter out = new PrintWriter(
        //                       raw_out, true);

        
            InputStream raw_in = this.client.getInputStream();
        //BufferedReader in = new BufferedReader(new InputStreamReader(raw_in));
        //String inputLine; //, outputLine=null;

        byte[] outputLine = null;
        // initiate conversation with client
        QRServiceProtocol qrp = new QRServiceProtocol();

        outputLine = qrp.processInput("");
        raw_out.write(outputLine);

        //int stream_pos = 0;
        //int byte_pos = 0;
        byte[] buffer = new byte[1024];
        //byte[] string_buffer = new byte[1024];

                
        int num_read = 0;
        boolean end_of_file = false;
        byte[] marker_buffer = new byte[5];
        byte[] end_marker = {10,70,79,69,10};

        int marker_read = 0;
        int data = 0;
        
        while(num_read != -1) {
            if(qrp.isBinaryMode()) {
                if(marker_read > 4) {
                    num_read = 4;
                }
                else {
                    num_read = 0;
                }
                while(!end_of_file && num_read < 1028 && data != -1) {
                    //forcing me to read byte by byte
                    data = raw_in.read();
                    
                    if(data == -1){
                        num_read = -1;
                        break;
                    }
                    num_read++;
                    marker_read++;
                
                    // for (int x=0; x<5; x++) {
                    //     System.out.println(((int) marker_buffer[x] & 0xFF)+" -- "+((char) marker_buffer[x]));
                    // }
                    //System.out.println((int) marker_buffer[4] & 0xFF);
                    if(marker_buffer[0] == 70 && marker_buffer[1] == 79 && marker_buffer[2] == 69) {
                        end_of_file = true;
                        break;
                    }
                    
                    //System.out.println("");
                    // slide them down
                    for(int j=4; j>0; j--) {
                        marker_buffer[j] = marker_buffer[j-1];
                    }
                    marker_buffer[0] = (byte) data;

                    if(num_read > 4) {
                        //System.out.println(num_read-5);
                        //System.out.println((int) marker_buffer[4] & 0xFF);
                        buffer[num_read-5] = (byte) marker_buffer[4];
                    }
                }

                //num_read = 1;
                //System.out.print("Buffer read "+(num_read-4));
                outputLine = qrp.processInput(buffer,num_read-4,end_of_file);
            }
            else {
                //System.out.println("Character "+((char) buffer[num_read-1]));
                num_read = raw_in.read(buffer);
                if(num_read > 1) {
                    outputLine = qrp.processInput(new String(buffer).substring(0,num_read-1));
                }
            }
            java.util.Arrays.fill(buffer, (byte) 0);
            //stream_pos += num_read;
            //System.out.println(stream_pos);
            //System.out.println(1024-(stream_pos+1));
            //System.out.println(new String(buffer));
            //System.out.println(num_read);
            //raw_out.write("Data".getBytes());

            if(new String(outputLine) != "") {
                    //System.out.println(new String(outputLine));
                    raw_out.write(outputLine);
             }
        
             if (new String(outputLine).equals("Bye.")) {
                 //this.client.close();
                 break;
             }
        // while ((inputLine = in.readLine()) != null) {    
        //     if(qrp.isBinary()) {
        // 
        //     }
        //     else {
        //         outputLine = qrp.processInput(inputLine);
        //     }
        //     
        //     if (outputLine.equals("Bye."))
        //         break;
        //     
        // }
        
    }//end while
    
    }//end try
    catch(IOException ioe) {
        System.out.println("I/O from socket failed");
        System.exit(-1);   
    }//end catch
    
    
    }// end run()
    
}// end class