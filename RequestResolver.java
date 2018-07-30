package MyChordPackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class RequestResolver {

	public static NodeCreator nodeCreator;
	public Scanner in;
	public Socket socket;
	public DataOutputStream dout;
	public int startingNode;
	public int startingSocket;
	
	
	public RequestResolver()
	{
		in=new Scanner(System.in);
	    nodeCreator=new NodeCreator();
	}
	
	//a method that informs the user what he is able to do
	public int interactWithUser()
	{
		System.out.println("Hello User! You can do:");
		System.out.println("1. set the first node of the  DHT");
		System.out.println("2. insert something in the database");
		System.out.println("3. query a node for something");
		System.out.println("4. delete something from the database");
		System.out.println("5. insert a new node");
		System.out.println("6. delete a node");
		System.out.println("7. proceed to question 1");
		System.out.println("8. proceed to question 2");
		System.out.println("9. proceed to question 3");
		System.out.println("10. exit and terminate");
		System.out.println("What do you want to do???");
		
		int userWishesTo=in.nextInt();
		in.nextLine();
		return userWishesTo;
	}
	
	//Depending on what the user wants, execute the appropriate method
	public void executeDependingOnOrder(int order)
	{
		int nodeNumber;
		String stringValue;
		String key;
		int value;
		String[] parts;
		
		switch(order)
		{
			case 1: //insert the first node in the DHT. There is special treatment for the first node
					System.out.println("Enter the nodeId of the first node in the DHT");
					nodeNumber=in.nextInt();
					in.nextLine();
					UsefulMethods.setStartingNode(nodeNumber);
					UsefulMethods.setStartingSocket(nodeNumber);
					execute1(nodeNumber);
					break;
			case 2: //insert a record in the system. The user just types what he wants to insert. 
				    //The placement in the appropriate node and the replication of the data does not concern the main program.
					System.out.println("What do you want to insert in the database?");
					stringValue=in.nextLine();
					parts=stringValue.split(", ");
					key=parts[0];
					value=Integer.parseInt(parts[1]);
					execute2(key,value);
					break;		
			case 3: //find if a record is stored in the system and if it is, get its value
					System.out.println("What do you want to learn from the database?");
					stringValue=in.nextLine();
					execute3(stringValue);
					break;					
			case 4: //delete a record from the system
					System.out.println("What do you want to delete from the database?");
					stringValue=in.nextLine();
					execute4(stringValue);
					break;		
			case 5: //create a new node. The responsibility for creating the new node belongs to the designated starting node.
					System.out.println("What is the node ID of the new node?");
	                nodeNumber=in.nextInt();
	                in.nextLine();
	                execute5(nodeNumber);
					break;					
			case 6: //delete a node from the system. The responsibility belongs to the designated starting node.
					System.out.println("You chose to delete a node. What node do you want to delete?");
					nodeNumber=in.nextInt();
					in.nextLine();
					execute6(nodeNumber);
					break;		
			case 7: execute7();
					break;
			case 8: execute8();
					break;
			case 9: execute9();
					break;
		
			case 10: //Terminate program if the user wants it.
					 execute10();
					 break;
			default: //if the input of the user is not 1 to 10, ask him to give a new input.
					 doNothing();
				     break;		     
		}
		return ;
	}
	
	//1.create the first node in the DHT
	private void execute1(int nodeName)
	{		
		int replicationFactor=UsefulMethods.getReplicationRate();
		nodeCreator.createNode(nodeName,replicationFactor);
		return ;
	}
	
	//2.insert something in the database
	private void execute2(String hashKey, int value)
	{
		nodeCreator.insertRecord(hashKey,value);
		String str=awaitMessageFromNode();
		String[] parts=str.split(" ");
		
		if(!parts[0].equals("committed"))
		{
			System.exit(1);
		}
		
		System.out.println(hashKey+" IS NOW INSERTED "+value);
		
		return ;
	}
	
	//3.learn something from the database
	private void execute3(String query)
	{
		nodeCreator.queryRecord(query);
		
		if(!query.equals("*"))
		{
			String str=awaitMessageFromNode();
			String[] parts;
			parts=str.split(" ");
		
			if(!parts[0].equals("committed"))
			{
				System.exit(1);
			}
			
		System.out.println(query+" RETURNED "+parts[1]);
		}
	}
	
	//4.delete something from the database
	private void execute4(String delete)
	{
		nodeCreator.deleteRecord(delete);
		String str=awaitMessageFromNode();
		if(!str.equals("committed"))
		{
			System.exit(1);
		}
	}
	
	//5.insert a new node with nodeID nodeName
	private void execute5(int nodeNumber)
	{		
		sendMessageToNode(UsefulMethods.getStartingSocket(),"insertNode "+nodeNumber);
		UsefulMethods.causeDelay(2000);
		sendMessageToNode(UsefulMethods.getStartingSocket(),"insertNodeReplicate "+nodeNumber);
		
		return ;
	}
	
	//6.delete the node with nodeID nodeName
	private void execute6(int nodeName)
	{
		sendMessageToNode(UsefulMethods.getStartingSocket(),"prepareToDeleteNode "+nodeName);
		return ;
	}
		
	//7.first question
	private void execute7()
	{
		long tStart = System.currentTimeMillis();
		
		FileReader fileReader = new FileReader();
		fileReader.openFile(1);
		
		String helpString=fileReader.readFile();
		
		while(!helpString.equals(""))
		{
			String[] parts;
			parts=helpString.split(", ");
			execute2(parts[0],Integer.parseInt(parts[1]));
			helpString=fileReader.readFile();
		}
		
		fileReader.closeFile();
		
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("TIME REQUIRED TO OPERATE FIRST QUESTION WAS "+elapsedSeconds);
		
		return ;
	}
	
	//8.second question
	private void execute8()
	{
		long tStart = System.currentTimeMillis();
		
		FileReader fileReader = new FileReader();
		fileReader.openFile(2);
		
		String helpString=fileReader.readFile();
		
		while(!helpString.equals(""))
		{
			execute3(helpString);
			helpString=fileReader.readFile();
		}
		
		fileReader.closeFile();
		
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("TIME REQUIRED TO OPERATE SECOND QUESTION WAS "+elapsedSeconds);
		
		return ;
	}
	
	//9.third question
	private void execute9()
	{
		long tStart = System.currentTimeMillis();
		
		FileReader fileReader = new FileReader();
		fileReader.openFile(3);
		
		String helpString=fileReader.readFile();
		
		while(!helpString.equals(""))
		{
			String[] parts;
			parts=helpString.split(", ");
			if(parts[0].equals("insert"))
			{
				execute2(parts[1],Integer.parseInt(parts[2]));
			}
			else if(parts[0].equals("query"))
			{
				execute3(parts[1]);
			}
			
			helpString=fileReader.readFile();
		}
		
		fileReader.closeFile();
		
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("TIME REQUIRED TO OPERATE THIRD QUESTION WAS "+elapsedSeconds);
		
		return ;
	}
	
	
	//10.exit. Terminate execution.
	private void execute10()
	{
		System.out.println("You chose to exit. Termination of program!");
		System.exit(0);
		return ;
	}
	
	//default:wrong input. Do nothing and ask again.
	private void doNothing()
	{
		System.out.println("Wrong answer!!!");
		System.out.println("What do you want to do?");
		return ;
	}
	
	//a method to send "message" to node with socket "socketNo"
	public void sendMessageToNode(int socketNo,String message)
	{
		try
		{
			socket=new Socket("localhost",socketNo);
			dout=new DataOutputStream(socket.getOutputStream());
			dout.writeUTF(message);
			dout.flush();
			dout.close();
			socket.close();
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION 2");
			System.out.println(e);
		}
	}
	
	public String awaitMessageFromNode()
	{
		String str="";
	
		try
		{
			ServerSocket serverSocket = new ServerSocket(UsefulMethods.getMasterSocket());
			Socket socket = serverSocket.accept();
			DataInputStream din = new DataInputStream(socket.getInputStream());
			str = (String)din.readUTF();
			serverSocket.close();
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION 3");
			System.out.println(e);
		}
		
		return str;
	}
	
}