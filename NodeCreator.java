package MyChordPackage;

import java.util.concurrent.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;


public class NodeCreator {

	public int nodeSocket;
	public ServerNode server;
	public int emptiness;
	public String nodeHashString;
	public BigInteger nodeHash;
	public static Hasher hasher;
	public int prior;
	public int next;
	public int isItEmpty;
	public int startingSocket;
	public int startingNode;
	public Socket socket;
	public DataOutputStream dout;
	public DataInputStream din;
	public String str;
	public ServerSocket serverSocket;
	public int currentStartingSocket;
	
	public BigInteger priorHash;
	public BigInteger nextHash;
	public BigInteger startingHash;
	public String nodeHashDelete;
	
	

	//a method to create a new node
	public void createNode(int nodeNumber, int replicationFactor)
	{		
		isItEmpty=UsefulMethods.getDhtEmptiness();
		
		nodeSocket=UsefulMethods.calculateSocketNumber(nodeNumber);
		nodeHashString=UsefulMethods.calculateHashValue(Integer.toString(nodeNumber));
		
		BigInteger nodeHash = new BigInteger(nodeHashString,16);
		
		if(isItEmpty==0) //if the system is empty, create the starting node
		{
			UsefulMethods.setDhtEmptiness(1);
			UsefulMethods.setStartingSocket(nodeNumber);
			UsefulMethods.setStartingNode(nodeNumber);
			UsefulMethods.setStartingHash(nodeHash);
			prior=nodeNumber;
			next=nodeNumber;
			priorHash=UsefulMethods.getStartingHash();
			nextHash=UsefulMethods.getStartingHash();
		}
		else if(UsefulMethods.getStartingHash().compareTo(nodeHash)<0)   //if the new node has ID number greater than 
		{																//the starting node
			prior=UsefulMethods.getStartingNode();
			priorHash=UsefulMethods.getStartingHash();
			next=UsefulMethods.getStartingNode();
			nextHash=UsefulMethods.getStartingHash();
		}
		else //if the new node has ID number lower than the starting node, the new node will become the starting node
		{
			next=UsefulMethods.getStartingNode();
			nextHash=UsefulMethods.getStartingHash();
			UsefulMethods.setStartingNode(nodeNumber);
			UsefulMethods.setStartingSocket(nodeNumber);
			UsefulMethods.setStartingHash(nodeHash);
			prior=nodeNumber;	
			priorHash=nodeHash;
		}
		
		//add the node to the list of nodes, so that we can pick a random node at insert, query, delete
		RandomNodePicker.addNodeToList(nodeNumber);
		
		//create the thread of the new node
		server = new ServerNode(nodeNumber,nodeSocket,nodeHash,prior,next,priorHash,nextHash,replicationFactor);
		//pool of threads
		ExecutorService threadExecutor = Executors.newCachedThreadPool();

		threadExecutor.execute(server);

		return ;
	}
	
	//a method to delete a node
	public void deleteNode(int nodeNumber, int nextOfStartingNode)
	{		
		isItEmpty=UsefulMethods.getDhtEmptiness();
		if(isItEmpty==0) 
		{
			System.out.println("System is empty. There are no nodes to delete");
		}
		else //are we deleting the starting node? Then, its next node will become the starting node
		{
			if(UsefulMethods.getStartingNode()==nodeNumber)
			{
				UsefulMethods.setStartingNode(nextOfStartingNode);
				UsefulMethods.setStartingSocket(nextOfStartingNode);
				nodeHashString=UsefulMethods.calculateHashValue(Integer.toString(nextOfStartingNode));
				UsefulMethods.setStartingHash(new BigInteger(nodeHashString,16));
			}
			
			//if we delete the last node, the system is empty.
			if(nextOfStartingNode==UsefulMethods.getStartingNode() && nodeNumber==UsefulMethods.getStartingNode())
			{
				UsefulMethods.setDhtEmptiness(0);
			}
			
			//remove the node from the random list		
			RandomNodePicker.removeNodeFromList(nodeNumber);
			//send message to next of the starting node to delete a node (why next?->otherwise, we cannot establish a connection with the starting node as we already have one
			sendMessageToNode(UsefulMethods.calculateSocketNumber(nextOfStartingNode),"deleteNode "+nodeNumber+" "+nextOfStartingNode);		
		}	
		return ;
	}
	
	//insert a new record
	public void insertRecord(String key,int value)
	{
		//start searching the right position from a random node
		int startSearchingFromNode=RandomNodePicker.pickRandomNumber();
		int startSearchingFromSocket=UsefulMethods.calculateSocketNumber(startSearchingFromNode);	
		String newHashValue=UsefulMethods.calculateHashValue(key);
				
		sendMessageToNode(startSearchingFromSocket,"insertRecord "+value+" "+newHashValue+" "+startSearchingFromNode);
		return ;
	}
	
	//search for a record
	public void queryRecord(String query)
	{
		//start searching the right position from a random node
		int startSearchingFromNode=RandomNodePicker.pickRandomNumber();
		int startSearchingFromSocket=UsefulMethods.calculateSocketNumber(startSearchingFromNode);
		if(!query.equals("*")) //print everything
		{
			String hashValueToFind=UsefulMethods.calculateHashValue(query);
			sendMessageToNode(startSearchingFromSocket,"queryRecord "+startSearchingFromNode+" "+hashValueToFind);
		}
		else //print what requested
		{
			sendMessageToNode(startSearchingFromSocket,"queryAll "+startSearchingFromNode);
		}
		return ;
	}
	
	//delete a record from the system
	public void deleteRecord(String delete)
	{
		int startSearchingFromNode=RandomNodePicker.pickRandomNumber();
		int startSearchingFromSocket=UsefulMethods.calculateSocketNumber(startSearchingFromNode);	
		String deleteHashValue=UsefulMethods.calculateHashValue(delete);
		
		sendMessageToNode(startSearchingFromSocket,"deleteRecord "+startSearchingFromNode+" "+deleteHashValue);
	}
	
	//a method to send a message to a node
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
			System.out.println(e);
		}
	}
	
}