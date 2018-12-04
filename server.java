import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.PrintStream;

import java.io.DataOutputStream;
import java.io.FileInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

class DataItem{
	private String user;
	private String pass;

	public DataItem(String use, String pwd) {
		user = use;
		pass = pwd;
	}

	public String getUser() {
		if(user == null)
			return null;
		else
			return user;
	}
	public String getPass() {
		return pass;
	}
}

class HashTable{
	private DataItem[] hashingTable;
	private int arraySize;

	public HashTable(int size) {
		arraySize = getPrime(2*size);
		hashingTable = new DataItem[arraySize];
	}

	private int getPrime(int min) {
		for(int j = min+1; true; j++)
			if(isPrime(j))
				return j;
	}
	private boolean isPrime(int n) {
		for(int j = 2; (j*j <= n); j++)
			if(n % j == 0)
				return false;
		return true;
	}

	public int hash(String key) {
		char oneChar;
		int hashVal = 0;

		if(key.length() > 0 && key.charAt(0) != '\n') {
			oneChar = key.charAt(0);
			key = key.substring(1);
			hashVal = ((int)oneChar) % arraySize;
		}
		while(key.length() > 0) {
			oneChar = key.charAt(0);
			key = key.substring(1);
			if(oneChar != '\n') {
				hashVal = (hashVal * 26 + (int)oneChar) % arraySize;
			}
		}
		return hashVal;
	}

	public void insertLinear(DataItem item){
	      String key = item.getUser();
	      int hashVal = hash(key);

	      if(hashingTable[hashVal] == null){
	    	  hashingTable[hashVal] = item;
	      }
	      else{
	         while(hashingTable[hashVal] != null && hashingTable[hashVal].getUser() != "-1" && !(hashingTable[hashVal].getUser().equals(item.getUser()))){
	            ++hashVal;
	            hashVal %= arraySize;
	         }
	         if(hashingTable[hashVal] == null){
	        	hashingTable[hashVal] = item;
	         }
	         else if(hashingTable[hashVal].getUser().equals(item.getUser())){
	            return;
	         }
	      }
	   }
	public String[] searchLinear(String key){
	      int i = hash(key);
	      String[] b = new String[3];

	      while(hashingTable[i] != null){
	         if(hashingTable[i].getUser().equals(key)){
	            b[0] = "1";
	            b[1] = key;
	            b[2] = hashingTable[i].getPass();
	            return b;
	         }
	         i = (i + 1) % (arraySize);
	      }
	      b[0] = "-1";
	      b[1] = key;
	      return b;
	   }
}

public class server{
  public static void main(String args[])  throws IOException {
		String s[] = null, command, username, password,
		 currentUser, currentPass, fileName, temp = null;
		boolean inSession = false, loggedIn = false, sending = false;
    int size = 0;
    DataItem aDataItem;
		File retrFile = new File("./root/");

    Scanner x = new Scanner(new File("./root/users.txt"));
    while(x.hasNext()) {
    	x.nextLine();
    	size++;
    }
    HashTable hashTable = new HashTable(size);

    x = new Scanner(new File("./root/users.txt"));
    while(x.hasNext()) {
    	currentUser = x.next();
    	currentPass = x.next();
    	aDataItem = new DataItem(currentUser, currentPass);
    	hashTable.insertLinear(aDataItem);
    }
		ServerSocket serverSocket = new ServerSocket(50002);

    Socket socket = serverSocket.accept();
		Scanner input = new Scanner(socket.getInputStream());
    PrintStream stream = new PrintStream(socket.getOutputStream());
    stream.println("S: Hello from VSFTP Service");
		inSession = true;

    command = input.next();
		while(inSession){
			if(command.equals("DONE")) {
				stream.println("DONE");
	    	stream.println("S: +GoodBye");
				inSession = false;
	    	stream.println(inSession);
				socket.close();
	    }
			while(!command.matches("USER|PASS|LIST|KILL|RETR|SEND|DONE")){
				stream.println("NOPE");
				stream.println("S: -Please use a valid command");
				stream.println(true);
				command = input.next();
			}
			while(loggedIn == false && !command.equals("USER") && !command.equals("DONE") ){
				stream.println("LOGFIRST");
				stream.println("S: -Please log in first");
				stream.println(true);
				command = input.next();
			}
	    while(command.equals("USER")) {
				stream.println("USER");
	    	username = input.next();
	    	s = hashTable.searchLinear(username);
				if(loggedIn == true)
					stream.println("S: -Already logged in");
				else{
	        if(s[0].equals("1"))
	            stream.println("S: +" + s[1] + " valid, send password");
	        else
	            stream.println("S: -Invalid user-id, try again");
				}
	        stream.println(true);
	        command = input.next();
	    }
	    while(command.equals("PASS")) {
				stream.println("PASS");
	    	password = input.next();
				if(loggedIn == true)
					stream.println("S: -Already logged in");
				else{
	      	if(password.equals(s[2])) {
	      		stream.println("S: ! logged in");
						loggedIn = true;
	      	}
	      	else
	      		stream.println("S: -Wrong password, try again");
				}
	      stream.println(true);
				command = input.next();
	    }
			while(command.equals("LIST")){
				stream.println("LIST");
				File[] files = new File("./root").listFiles();
				stream.println(files.length);
				stream.print("S: +");
				for(int i = 0; i < files.length; i++){
					if(i < files.length - 1)
						stream.println(files[i].getName() + "\n");
					else
						stream.println(files[i].getName() + "\n");
				}
				stream.println(true);
				command = input.next();
			}
			while(command.equals("KILL")){
				stream.println("KILL");
				fileName = input.next();
				File file = new File("./root/" + fileName);
				if(file.delete())
					stream.println("S: +" + fileName + " deleted.");
				else
					stream.println("S: -Not deleted because file does not exist");
				stream.println(true);
				command = input.next();
			}
			while(command.equals("RETR")){
				stream.println("RETR");
				fileName = input.next();
				retrFile = new File("./root/" + fileName);
				if(retrFile.exists()){
					stream.println(retrFile.length());
					stream.println("S: #" + retrFile.length());
					sending = true;
				}else
					stream.println("S: -File does not exist");
				stream.println(true);
				command = input.next();
			}
			while(command.equals("STOP")){
				stream.println("STOP");
				stream.println("S: +ok, RETR aborted");
				stream.println(true);
				command = input.next();
			}
			while(command.equals("SEND")){
				if(sending == false){
					stream.println("BADSEND");
					stream.println("S: -Must use RETR first");
				}else{
					stream.println("GOODSEND");
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					FileInputStream fis = new FileInputStream(retrFile);
					byte[] buffer = new byte[1024];
					while(fis.read(buffer) > 0){
						dos.write(buffer);
					}
					if(retrFile.length() > 1024) stream.println("S: -Filesize is too large, aborting");
					else if(retrFile.length() < 512) stream.println("S: +Filesize is small, sending");
					else stream.println("S: +Filesize is large, sending");
				 }
					stream.println(true);
					command = input.next();
				}
			}
		}

  }
