//A class containing some methods that will make the program easier to write. 
//Some methods might seem to violate the idea of P2P system.
//However, each of these methods could be implemented in a distributed way by sending messages between nodes of the system.
//Also, none of these methods would be necessary if the starting node was chosen to be 1

package MyChordPackage;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class UsefulMethods {

	public static BigInteger startingHash;
	public static int startingNode;
	public static int startingSocket;
	public static int emptiness;
	public static Hasher hasher;
	public static int replicationRate;
	public static Scanner in;
	public static String consistencySystem;
	
	
		//set the socket of the main program
		public static int getMasterSocket()
		{
			return 30000;
		}
	
		//set the replication factor of the system
	    public static void setReplicationRate(int k)
	    {
	    	replicationRate=k;
	    }
	    
	    //get the replication factor of the system
	    public static int getReplicationRate()
	    {
	    	return replicationRate;
	    }		
		
	    //set the SHA1 hash value of the starting node
		public static void setStartingHash(BigInteger number)
		{
			startingHash=number;
			return ;
		}
		
		//get the SHA1 hash value of the starting node
		public static BigInteger getStartingHash()
		{
			return startingHash;
		}
		
		//set the socket of the first node
		public static void setStartingSocket(int nodeNumber)
		{
			startingSocket=nodeNumber+20000;
		}
		
		//get the socket of the first node
		public static int getStartingNode()
		{
			return startingNode;
		}
		
		//set the starting node
	    public static void setStartingNode(int number)
	    {
	    	startingNode=number;
	    }

	    //get the starting node
		public static int getStartingSocket()
		{
			return startingSocket;
		}
		
		//a method to calculate the socket number from the node ID
		public static int calculateSocketNumber(int nodeNumber)
		{
			return (20000+nodeNumber);
		}
		
		//does the system have at least one node? Yes=1 and No=0
		public static void setDhtEmptiness(int choice)
		{
			if (choice==0)
			{
				emptiness=0;
			}
			else
			{
				emptiness=1;
			}
		}
		
		//find if the system is empty
		public static int getDhtEmptiness()
		{
			return emptiness;
		}
		
		
		public static void setConsistencySystem(String choice)
		{
			if(choice.equals("eventual"))
			{
				consistencySystem="eventual";
			}
			else if(choice.equals("linearizability"))
			{
				consistencySystem="linearizability";
			}
			else
			{
				System.out.println("Wrong input at selection of consistency system");
				System.exit(1);
			}
			return ;
		}
		
		
		public static String getConsistencySystem()
		{
			return consistencySystem;
		}
		
		public static void requestConsistencySystem()
		{
			System.out.println("Choose consistency system");
			System.out.println("For eventual consistency type: eventual");
			System.out.println("For linearizability typer: linearizability");
					
			in = new Scanner(System.in);
			String choice=in.nextLine();
			
			setConsistencySystem(choice);
		}
		
		//a method to calculate the hash value of a string
		public static String calculateHashValue(String nodeNumber)
		{    
			String result;
			hasher=new Hasher();
		
			try{
				result=hasher.returnHashString(nodeNumber);
			}
			catch(NoSuchAlgorithmException e)
			{
				System.out.println(e);
				result="";
			}
		
			return result;
		}
	
		//a method to ask for the replication rate of the system
		public static  void requestReplicationRate()
		{
			System.out.println("What is the replication rate?");
		
			in = new Scanner(System.in);
			int k=in.nextInt();
			in.nextLine();
				
			UsefulMethods.setReplicationRate(k);
		}
		
		//a method to delay a program
		public static void causeDelay(int msec)
		{
			try
			{
				Thread.sleep(msec);
			}
			catch(Exception e)
			{
				System.out.println(e);
			} 
		}
		
}
