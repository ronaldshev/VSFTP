import java.util.Scanner;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

import java.io.DataInputStream;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class client{
  public static void main(String args[]) throws UnknownHostException, IOException {
	  boolean works = true;
	  String data;
    int listSize, fileSize = 0, size = 1024, bytesRead, current = 0;
    System.out.println("S: (listening for connection)");
    try{
      Scanner input = new Scanner(System.in);
      Socket socket = new Socket("127.0.0.1",50002);
      Scanner serverInput = new Scanner(socket.getInputStream());
      PrintStream stream = new PrintStream(socket.getOutputStream());

    System.out.println("C: (opens connection to R)");
    System.out.println(serverInput.nextLine());
    while(works == true){
    	System.out.print("C: ");
    	data = input.nextLine();
    	stream.println(data);
      data = serverInput.nextLine();
      if(data.equals("LIST")){
        listSize = Integer.parseInt(serverInput.nextLine());
        System.out.print(serverInput.next() + " ");
        for(int i = 0; i < listSize; i++){
          if(i == 0)
            System.out.println(serverInput.next());
          else
            System.out.println("   " + serverInput.next() + "\t");
        }
      }else if(data.equals("RETR")){
        fileSize = Integer.parseInt(serverInput.nextLine());
        System.out.println(serverInput.nextLine());
      }else if(data.equals("GOODSEND")) {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream("./root/file.txt");
        byte[] buffer = new byte[size];
        int read = 0, totalRead = 0, remaining = fileSize;
        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0){
          totalRead += read;
          remaining -= read;
          fos.write(buffer, 0, read);
        }
        System.out.println(serverInput.nextLine().trim());
      }else System.out.println(serverInput.nextLine());
      if(data.equals("GOODSEND")){
        works = Boolean.parseBoolean(serverInput.next());
      }
      else works = serverInput.nextBoolean();
      serverInput.nextLine();
    }
    socket.close();
    }
    catch(Exception e){
      System.out.println("-VSFTP Server is out for lunch");
    }
  }
}
