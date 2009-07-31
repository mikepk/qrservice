//package com.google.zxing.client.j2se;

import com.google.zxing.DecodeHintType;
import com.google.zxing.MonochromeBitmapSource;
import com.google.zxing.MultiFormatReader;

import com.google.zxing.qrcode.QRCodeReader;

// My hack adds
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;

import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
//end

import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Hashtable;

import com.google.zxing.client.j2se.BufferedImageMonochromeBitmapSource;

import java.net.*;
import java.io.*;

public class QRServiceProtocol {
    private static final int WAITING = 0;
    private static final int COMMAND = 1;
    private static final int DATA = 2;
    private static final int ANOTHER = 3;

    private static final int NONE = 1;
    private static final int ENCODE = 1;
    private static final int DECODE = 2;

    
    private int state = WAITING;

    private int command = NONE;

    private String[] commands = {"encode","decode","done"};

    private byte[] data_buffer = new byte[1024*500];
    private int buffer_pos = 0;

    private boolean input_binmode = false;
    private boolean output_binmode = false;

    public boolean isBinaryMode() {
        return input_binmode;
    }


    public boolean isBinaryOutputMode() {
        return output_binmode;
    }


    // public byte[] processInput(byte[] raw_data) {
    //     byte[] data = new byte[10];
    //     return data;
    // }


    public byte[] processInput(byte[] raw_data, int amount, boolean eof) {
        if(raw_data.length == 0) {
            return "".getBytes();
        }
        String theOutput = "QRService v0.1";
        //System.out.println("Binary process mode?");
        //System.out.println(new String(raw_data));
        
        //System.out.println("buffer position:"+buffer_pos);
        System.arraycopy(raw_data,0,data_buffer,buffer_pos,amount);
        buffer_pos += amount;
        //data_buffer = data_buffer.concat(theInput+"\n");
        theOutput = "";
        //System.out.println(".");
        
        //System.out.println("num_read :"+amount);
        //System.out.println(raw_data.length);
        //String temp = new String(raw_data);
        //System.out.println(temp.length());
        
        // if(amount > 9) {
        //     for(int i=amount-10; i < amount; i++) {
        //         System.out.println("DATA :"+((int) raw_data[i] & 0xFF)+" "+((char) raw_data[i]));
        //     }
        //     // byte[] marker = new byte[10];
        //     // System.arraycopy(raw_data,amount-10,marker,0,10);
        //     // System.out.println(new String(marker));
        // }
        //System.out.println("DATA :"+temp.substring(temp.length()-20,temp.length()-1)); //new String(raw_data).length()); //substring(amount-4,3));
        if(eof) { //new String(raw_data).substring(0,3).equalsIgnoreCase("EOF")) {
            try{
                //System.out.println("DATA TRANSFER COMPLETE");
                theOutput = QRDecodeByteString(data_buffer);
                input_binmode = false;
                state = COMMAND;
                command = NONE;
            }
            catch(Exception e) {
                theOutput = e+" Data Error";
                input_binmode = false;
                state = COMMAND;
                command = NONE;
            }
            
            
        }
        // else {
        //     System.out.println(buffer_pos);
        //     System.arraycopy(raw_data,0,data_buffer,buffer_pos,amount);
        //     buffer_pos += amount;
        //     //data_buffer = data_buffer.concat(theInput+"\n");
        //     theOutput = "";
        //     System.out.println(".");
        // }


        return theOutput.getBytes();
    }


    public byte[] processInput(String theInput) {
        String theOutput = "QRService v0.1";
        byte[] rawOutput = null;
        
        //System.out.println(theInput.length());
        //System.out.print(theInput+"------");
        //System.out.print(theInput.equalsIgnoreCase("done"));
        if (state == WAITING) {
            theOutput = "QRService v0.1";
            state = COMMAND;
        }
        else if (state == COMMAND) {
            if (theInput.equalsIgnoreCase("encode")) {
                theOutput = "data"; //clues[currentJoke];
                command = ENCODE;
                state = DATA;
            }
            else if (theInput.equalsIgnoreCase("decode")) {
                //System.out.println("We're in the decode mode");
                theOutput = "data";
                input_binmode = true;
                command = DECODE;
                state = DATA;
            }
            else if (theInput.equalsIgnoreCase("done")) {
                theOutput = "Bye.";
                state = WAITING;
            } else {                
                theOutput = "QRService v0.1 - "+theInput+" Command Not Recognized\n";
            }
        }
        else if (state == DATA) {
            // DO SOMETHING WITH IT
            
            if(command == ENCODE) {
                try {
                    rawOutput = QREncodeString(theInput);
                    output_binmode=true;
                    state = COMMAND;
                    return rawOutput;
                }
                catch (IOException ie) {
                    theOutput = ie+"Data Error";
                }
                catch (URISyntaxException urie) { 
                    theOutput = urie+" Data Error";
                }
                catch (WriterException we) {
                    theOutput = we+" Data Error";
                }
                    state = COMMAND;
            }
            // else if(command == DECODE) {
            //     try {
            // 
            //         // if(theInput.equalsIgnoreCase("EOF")) {
            //         //     System.out.println("DATA TRANSFER COMPLETE");                        
            //         //     theOutput = QRDecodeByteString(data_buffer);
            //         //     data_buffer = "";
            //         //     state = COMMAND;
            //         // }
            //         // else {
            //         //     //System.out.println(theInput);
            //         //     data_buffer = data_buffer.concat(theInput+"\n");
            //         //     theOutput = "";
            //         //     System.out.println(".");
            //         // }   
            //     }
            //     catch(Exception e) {
            //         theOutput = e+" Data Error";
            //         state = COMMAND;
            //     }
            // }
        }
        return theOutput.getBytes();
    }
    
    
    private static byte[] QREncodeString(String data) throws IOException, URISyntaxException, WriterException {

        int height = 100;
        int width = 100;

        //ByteMatrix result = new QRCodeWriter().encode(data,BarcodeFormat.QR_CODE,height,width);

        QRCode code = new QRCode();
        code.setVersion(3);
        Encoder.encode(data, ErrorCorrectionLevel.H, code);

        //return renderResult(code, width, height);


        int i=0;
        int j=0;

        ByteMatrix result = code.getMatrix();
        byte[][] output = result.getArray();


        //System.out.println(code.getECLevel());
        //System.out.println(code.getVersion());

        int pad_mult = 1;
        int s = 0;
        int t = 0;
        char c = ' ';
        byte byte_val = 0;

        // int bitpos = 0;
        // byte data_byte = 0;


        int total_bits = (pad_mult * output.length) * (pad_mult * output[0].length);
        //System.out.println("Total "+total_bits);


        byte[] header = new byte[64];
        // create the header
        String text_header = "QR\n"+Integer.toString(output.length * pad_mult)+"\n"+Integer.toString(output.length * pad_mult)+"\n";
        byte[] temp_header = text_header.getBytes();


        for(int x=0; x<temp_header.length; x++) {
            header[x] = temp_header[x];
        }

        byte[] stream = new byte[total_bits];

        byte[] file_data = new byte[header.length + stream.length];


        int stream_pos=0;
        // for(int blh=0; blh<output.length; blh++) {
        //     stream[stream_pos++] = (byte) 255;
        // }

        for(j=0; j<output.length; j++) { 
            for(s=0; s < pad_mult; s++) {
                // start row
                //stream[stream_pos++] = (byte) 255;
                for(i=0; i<output[j].length; i++){
                    if(((int) output[j][i] & 0xFF) == 0) {
                         c = ' '; //System.out.print(" ");
                         byte_val = (byte) 255;
                    } 
                    else {
                        c = 'X';                      
                        byte_val = (byte) 0;

                    }
                    for(t=0;t < pad_mult;t++) {
                        //System.out.print(c);


                        stream[stream_pos++] = byte_val;

                    }

                    //stream[stream_pos++] = (byte) ((int) (output[j][i]) * 255);
                }
            // end row
            //stream[stream_pos++] = (byte) 255;
            //System.out.print("\n");
            }
        }

        // for(int blf=0; blf<output.length; blf++) {
        //     stream[stream_pos++] = (byte) 255;
        // }


        //forcibly concatenate the header + the stream (annoying)
        int headerx_pos = 0;
        int streamx_pos = 0;
        for(int z=0; z<file_data.length;z++) {
            if(z < header.length) {
                file_data[z] = header[headerx_pos++];
            }
            else {
                file_data[z] = stream[streamx_pos++];
            }
        }


        // for(j=0; j<file_data.length; j++) {
            //System.out.print((int) file_data[j] & 0xFF);
        // }
        // System.out.print("\n");
        
        return file_data;

        //File dump = new File("img_test.qr");
        //FileOutputStream out = new FileOutputStream(dump);
        //out.write(file_data);
        //writeStringToFile(stream, dump);

        //encode(String contents, BarcodeFormat format, int width, int height)
    }

    //, Hashtable<DecodeHintType, Object> hints
    private static String QRDecodeByteString(byte[] bin_data) throws IOException {
        //System.out.println("Trying to decode?");
        File dump = new File("img_test.png");
        //byte[] bin_data = 
        FileOutputStream out = new FileOutputStream(dump);
        out.write(bin_data);

      BufferedImage image;
      Hashtable<DecodeHintType, Object> hints = null;
      //System.out.print(data);
      ByteArrayInputStream bs = new ByteArrayInputStream(bin_data);

      try {
        image = ImageIO.read(bs);
      } catch (IllegalArgumentException iae) {
        throw new FileNotFoundException("Resource not found");
      }

      if (image == null) {
          throw new FileNotFoundException("Could not load image");
      }
      try {

        MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource(image);
        Result result = new QRCodeReader().decode(source, hints);
        ParsedResult parsedResult = ResultParser.parseResult(result);
        //System.out.println("Raw result:" + result.getText() +"\n");

        //          "\nParsed result:\n" + parsedResult.getDisplayResult());

        // System.out.println(uri.toString() + " (format: " + result.getBarcodeFormat() +
        //     ", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() +
        //     "\nParsed result:\n" + parsedResult.getDisplayResult());
        //System.out.println(result.getText());
        return result.getText();
      } catch (ReaderException e) {
        //System.out.println("No barcode found");
        return "NO BARCODE";
      }
    }

        
    
}