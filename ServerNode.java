//The code every node runs

package MyChordPackage;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.math.BigInteger;


public class ServerNode implements Runnable {
	
	public int nodeNumber;
	public int nodeSocket;
	public BigInteger nodeHash;
	public int prior;
	public int next;
	public BigInteger priorHash;
	public BigInteger nextHash;
	public BigInteger startingHash;
	public ServerSocket serverSocket;
	public Socket socket;
	public DataInputStream din;
	public String str;
	public String order;
	public int argument1;
	public BigInteger argument2;
	public int argument2Int;
	public BigInteger argument3;
	public BigInteger argument4;
	public DataOutputStream dout;
	public int senderSocketNumber;
	public int help;
	public static NodeCreator nodeCreator;
	public int startingSocket;
	public int startingNode;
	public BigInteger recordHashValue;
	public BigInteger hashBigInteger;
	public BigInteger priorHashValue;
	public int recordValue;
	public int replicationFactor;
	
	public Hashtable<String,Integer> authorityHashtable = new Hashtable<String,Integer>();
	public Hashtable<String,Integer> hashtable = new Hashtable<String,Integer>();


	//constructor. Prior points to the previous node and next points to the next node.
	//The only difference is in the first and last node
	//The first node has prior equal to its nodeID and the last node has next equal to ID of the first node
	public ServerNode(int nodeNumber,int nodeSocket,BigInteger nodeHash,int prior,int next,BigInteger priorHash,BigInteger nextHash,int replicationFactor)
	{
		this.nodeNumber=nodeNumber;
		this.nodeSocket=nodeSocket;
		this.nodeHash=nodeHash;
		this.prior=prior;
		this.next=next;		
		this.priorHash=priorHash;
		this.nextHash=nextHash;
		this.replicationFactor=replicationFactor;
	}
	
	public void run() //code of the thread
	{		
		System.out.println("Node "+nodeNumber+" has been created");
		//what is the first node in the Chord circle, its hash value and its socket
		startingNode=prior;
		startingHash=priorHash;
		startingSocket=calculateSocketNumber(prior);
				
		//if we insert a node with ID less than the startingNode, we have to change the startingNode
		//and update the value startingNode in every node in the circle so that everyone knows who is the first node in the circle
		if(prior!=next)
		{
			sendMessageToNode(calculateSocketNumber(next),"changePrior "+startingNode+" "+startingHash);
			sendMessageToNode(calculateSocketNumber(next),"changeStartingNode "+startingNode+" "+startingHash);
		}
		
		nodeCreator=new NodeCreator();
		
		//we inserted at least one node. The circle is not empty now. Number 1 symbolizes the non empty circle.
		UsefulMethods.setDhtEmptiness(1);
		
//		System.out.println("I am node "+nodeNumber+"with prior "+prior+" "+next+" priorHash "+priorHash+" "+nextHash);		
		
	   try{
		   String[] parts;
		   
		   //the socket that the server is listening to
		   serverSocket = new ServerSocket(nodeSocket);
		  
		   //check if this node is the first node in Chord circle
		   //if it is, do nothing
		   //if it is not, send messages to calculate prior and next again, starting from the first node
		   if(prior==next && prior==startingNode && prior!=nodeNumber)
			{
   			    help=startingSocket;
				sendMessageToNode(help,"recalculatingPriorNext "+nodeNumber+" "+nodeHash);
			}
		   
		  //now the node is ready to accept requests 
		   while(true)
		   {
//			   System.out.println("I am node "+nodeNumber+"with prior "+prior+" "+next+" and starting node is "+startingNode);

			   //wait for a node to send a request
			   socket = serverSocket.accept();
			   din = new DataInputStream(socket.getInputStream());
			   str = (String)din.readUTF();
			   //split the message of the other nodes with respect to space
			   parts=str.split(" ");
			   //zero element is the request and the next elements are the details
			   order=parts[0];
		  
			   if(order.equals("recalculatingPriorNext"))//calculate prior and next again for this node
			   {
				   argument1=Integer.parseInt(parts[1]); //argument1 is the number of the node that sent the message
				   argument2=new BigInteger(parts[2]);  //argument2 is the hash value of the node that sent the message
				   
				   if(prior==next && prior==nodeNumber)//for the first node in the topology when it is alone
				   {								   
					   next=argument1;
					   nextHash=argument2;
					   senderSocketNumber=UsefulMethods.calculateSocketNumber(argument1);
					   sendMessageToNode(senderSocketNumber,"changePrior "+nodeNumber+" "+nodeHash);
				   }
				   else 
				   {
					   if(next==startingNode) //for the last node in the topology. Its next must be the starting node.
					   {
						   next=argument1;
						   nextHash=argument2;
						   senderSocketNumber=UsefulMethods.calculateSocketNumber(argument1);
						   sendMessageToNode(senderSocketNumber,"changePrior "+nodeNumber+" "+nodeHash);
					   }
				       else if(nextHash.compareTo(argument2)<0) //for middle nodes when we have to go to the next node. We have to compare the hash values of the IDs of the nodes.
					   {
						   senderSocketNumber=UsefulMethods.calculateSocketNumber(next);
						   sendMessageToNode(senderSocketNumber,"recalculatingPriorNext "+argument1+" "+argument2);
					   }
					   else //for middle nodes when the new node must be placed between them
					   {
						   senderSocketNumber=UsefulMethods.calculateSocketNumber(argument1);
						   sendMessageToNode(senderSocketNumber,"changeBoth "+nodeNumber+" "+next+" "+nodeHash+" "+nextHash);
						   senderSocketNumber=UsefulMethods.calculateSocketNumber(next);
						   sendMessageToNode(senderSocketNumber,"changePrior "+argument1+" "+argument2);
						   next=argument1;
						   nextHash=argument2;
					   }
				   }
				   
			   }
			   else if(order.equals("changePrior"))//change prior of the node. The prior node will be argument1 and priorHashValue will be argument2.
			   {
				   argument1=Integer.parseInt(parts[1]);
				   argument2=new BigInteger(parts[2]);
				   prior=argument1;
				   priorHash=argument2;		   
			   }
			   else if(order.equals("changeNext"))//change next of the node. The new next will be argument1 node and nextHashValue will be argument2.
			   {
				   argument1=Integer.parseInt(parts[1]);
				   argument2=new BigInteger(parts[2]);
				   next=argument1;
				   nextHash=argument2;
			   }
			   else if(order.equals("changeBoth"))//change both prior and next of the node. Set prior to node argument1 and next to node argument2.
			   {
				   argument1=Integer.parseInt(parts[1]);
				   argument2Int=Integer.parseInt(parts[2]);
				   argument3=new BigInteger(parts[3]);
				   argument4=new BigInteger(parts[4]);
				   prior=argument1;
				   priorHash=argument3;
				   next=argument2Int;
				   nextHash=argument4;
			   }
			   else if(order.equals("returnNext"))//learn the next node of a node
			   {
				   argument1=Integer.parseInt(parts[1]);
				   sendMessageToNode(argument1,Integer.toString(next));
			   }
			   else if(order.equals("changeStartingNode"))//change the starting node in case that the new node must become the first node
			   {
				   argument1=Integer.parseInt(parts[1]);
				   argument2=new BigInteger(parts[2]);
				   if(next!=startingNode) //if not the last node
				   {
					   startingNode=argument1;
					   startingHash=argument2;
					   sendMessageToNode(calculateSocketNumber(next),"changeStartingNode "+startingNode+" "+startingHash);
				   }
				   else //in case we reach the last node in topology
				   {
					   startingNode=argument1;
					   startingHash=argument2;
					   next=startingNode;
					   nextHash=startingHash;
				   }   				   
			   }
			   else if(order.equals("deleteNode")) //in order to delete a node
			   {
				   Thread.sleep(1000);
				   argument1=Integer.parseInt(parts[1]); //argument1 is the node to be deleted
				   int nextOfStarting=Integer.parseInt(parts[2]); //the next of the starting node. We need it to stop the process if there is not a node argument1 in the circle.
				   
				   if(nodeNumber==argument1)//if this is the node we want to delete
				   {
					   if(nodeNumber==prior && prior==next) //if there is only one node in DHT
					   {
						   break;
					   }
					   else if(nodeNumber==startingNode)//if we want to delete the first node
					   {
						   sendMessageToNode(calculateSocketNumber(next),"changePrior "+next+" "+nextHash);
						   Thread.sleep(1000);
						   sendMessageToNode(calculateSocketNumber(next),"changeStartingNode "+next+" "+nextHash);
						   break;
					   }
					   else if(next==startingNode)//if we want to delete the last node
					   {
						   sendMessageToNode(calculateSocketNumber(prior),"changeNext "+startingNode+" "+startingHash);
						   Thread.sleep(1000);
						   break;
					   }
					   else //if we want to delete a middle node
					   {
						   sendMessageToNode(calculateSocketNumber(prior),"changeNext "+next+" "+nextHash);
						   sendMessageToNode(calculateSocketNumber(next),"changePrior "+prior+" "+priorHash);
						   break;
					   }
				   }
				   else { //this is not the node we want to delete
					   if(next==nextOfStarting)
					   {
						   System.out.println("There is no such node. Nothing to delete");
					   }
					   else //keep looking for the node we want to delete
					   {
						   sendMessageToNode(calculateSocketNumber(next),"deleteNode "+argument1+" "+nextOfStarting);
					   }
				   }
			   }
			   else if(order.equals("insertRecord")) //part to insert a record in the circle
			   {
				   recordHashValue=new BigInteger(parts[2],16);
				   String recordHashValueString=recordHashValue.toString();
				   int startSearchingFromNode=Integer.parseInt(parts[3]);  //we begin searching from a random node.
				   recordValue=Integer.parseInt(parts[1]);
				   	   
				   int comparisonLower=priorHash.compareTo(recordHashValue); //some comparisons to determine where the record will be stored.
				   int comparisonUpper=recordHashValue.compareTo(nodeHash);
				   int comparisonFinal=nodeHash.compareTo(recordHashValue);  //for the special case when the record belongs to the first node. 
				   int comparisonNext=recordHashValue.compareTo(nextHash);  //There, the hash value of the last node is not less than the hash value of the first node.
				   
				   
				   if(nodeNumber==prior && prior==next)  //if there is only one node in the system, just store the record to it.
				   {
					 hashtable.put(recordHashValueString, recordValue);
					 System.out.println("record added to starting node");
				   }
				   else //if there are more than one nodes in the system
				   { 
					   if(comparisonLower<0 && comparisonUpper<=0 && nodeNumber!=startingNode)  //if it is the usual case. The hash value of the record is between the values 
					   {																		//of the prior and last value.
						   hashtable.put(recordHashValueString, recordValue);
						   authorityHashtable.put(recordHashValueString, replicationFactor); //for replication=5, 5 points the head of the replication system and 1 the tail
						   
						   if(UsefulMethods.getConsistencySystem().equals("eventual"))
						   {
							   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+nodeNumber);
						   }
						   
						   
						   if(next!=nodeNumber && (replicationFactor-1)!=0) //if we still have to replicate.
						   {
							    System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));
						   		sendMessageToNode(calculateSocketNumber(next),"replicateInsert "+recordHashValueString+" "+recordValue+" "+(replicationFactor-1)+" "+nodeNumber);
						   }
						   else //if this is the tail in the replication system
						   {						   		
							   authorityHashtable.put(recordHashValueString, 1);
							   System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));
							   if(UsefulMethods.getConsistencySystem().equals("linearizability") && replicationFactor==1)
							   {
								   System.out.println("PRINT HELLO 2");
								   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+nodeNumber);
							   } 
						   }
						   
					   }
					   else if((comparisonFinal<0 || comparisonNext<0) && next==startingNode) //for the special case as said above
					   {
						   sendMessageToNode(calculateSocketNumber(next),"storeRecord "+recordHashValue+" "+recordValue+" "+nodeNumber);
					   }
					  
					   else //no match. Go on searching for the right node to store the record.
					   {
						   sendMessageToNode(calculateSocketNumber(next),"insertRecord "+recordValue+" "+parts[2]+" "+startSearchingFromNode);
					   } 
				   }
			   }
			   else if(order.equals("replicateInsert"))  //code to help replication.
			   {
				   recordHashValue=new BigInteger(parts[1]);
				   String recordHashValueString=recordHashValue.toString();
				   recordValue=Integer.parseInt(parts[2]);
				   int leftToReplicate=Integer.parseInt(parts[3]);
				   int authoritative=Integer.parseInt(parts[4]);
				   
				   hashtable.put(recordHashValueString, recordValue);
				   
				   	if(next!=authoritative && (leftToReplicate-1)!=0) //if this is not the tail of the replication system.
				   	{
				   		authorityHashtable.put(recordHashValueString, leftToReplicate);
				   		sendMessageToNode(calculateSocketNumber(next),"replicateInsert "+recordHashValueString+" "+recordValue+" "+(leftToReplicate-1)+" "+authoritative);
				   		System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));
				   	}
				   	else //if this is the tail of the replication system.
				   	{
				   		authorityHashtable.put(recordHashValueString, 1);
				   		System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));
				   		if(UsefulMethods.getConsistencySystem().equals("linearizability"))
				   		{
				   			sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+nodeNumber);
				   		}    	
				   	}
			   }
			   else if(order.equals("replicateInsertDelete"))
			   {
				   recordHashValue=new BigInteger(parts[1]);
				   String recordHashValueString=recordHashValue.toString();
				   recordValue=Integer.parseInt(parts[2]);
				   int leftToReplicate=Integer.parseInt(parts[3]);
				   int authoritative=Integer.parseInt(parts[4]);
				   
				   hashtable.put(recordHashValueString, recordValue);
				   
				   	if(next!=authoritative && (leftToReplicate-1)!=0) //if this is not the tail of the replication system.
				   	{
				   		authorityHashtable.put(recordHashValueString, leftToReplicate);
				   		sendMessageToNode(calculateSocketNumber(next),"replicateInsertDelete "+recordHashValueString+" "+recordValue+" "+(leftToReplicate-1)+" "+authoritative);
				   		System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));
				   	}
				   	else //if this is the tail of the replication system.
				   	{
				   		authorityHashtable.put(recordHashValueString, 1);
				   		System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(recordHashValueString));  	
				   	}
			   }
			   else if(order.equals("storeRecord")) //for the special case that the record belongs to the first node.
			   {			
				   recordValue=Integer.parseInt(parts[2]);
				   hashtable.put(parts[1], recordValue);
				   int authoritative=Integer.parseInt(parts[3]);
				   
				   if(UsefulMethods.getConsistencySystem().equals("eventual"))
			   	   {
			   			sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+nodeNumber);
				   }
				   
				   if(next!=authoritative && (replicationFactor-1)!=0) //if this is not the tail.
				   {
				   		authorityHashtable.put(parts[1], replicationFactor);
				   		sendMessageToNode(calculateSocketNumber(next),"replicateInsert "+parts[1]+" "+recordValue+" "+(replicationFactor-1)+" "+authoritative);
				   		System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(parts[1]));
				   }
				   else //if this is the tail.
				   {
					   authorityHashtable.put(parts[1], 1);	
					   
					   if(UsefulMethods.getConsistencySystem().equals("linearizability"))
				   		{
				   			sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+nodeNumber);
						}
					   
					   System.out.println(recordValue+ " is now stored in node "+nodeNumber+" with head "+authorityHashtable.get(parts[1]));
				   }
				   
			   }
			   else if(order.equals("queryRecord"))  //if we want to search for a record.
			   {
				   int startSearchingFromNode=Integer.parseInt(parts[1]);
				   hashBigInteger=new BigInteger(parts[2],16);
				   String hashToFind=hashBigInteger.toString();			   
				   
				   if(UsefulMethods.getConsistencySystem().equals("linearizability") && hashtable.containsKey(hashToFind)==true && authorityHashtable.get(hashToFind)==1) //get the record value only from the tail.
				   {
						  System.out.println("At node "+nodeNumber+" value "+ hashtable.get(hashToFind)); 
						  sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+hashtable.get(hashToFind));
				   }
				   else if(UsefulMethods.getConsistencySystem().equals("eventual") && hashtable.containsKey(hashToFind)==true)
				   {
					    System.out.println("At node "+nodeNumber+" value "+ hashtable.get(hashToFind)); 
						sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+hashtable.get(hashToFind));
				   }
				   else
				   {
					   if(startSearchingFromNode==next) //to stop the process if the value does not exist.
					   {
						   System.out.println("No such element in DHT");
						   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed "+hashtable.get(hashToFind));
					   }
					   else //if we do not find the value, go on searching.
					   {
						   sendMessageToNode(calculateSocketNumber(next),"queryRecord "+startSearchingFromNode+" "+parts[2]);
					   }
				   }
			   }
			   else if(order.equals("queryAll")) //in case we want to print all records.
			   {			
				   int startSearchingFromNode=Integer.parseInt(parts[1]);

				   Enumeration<String> e_keys = hashtable.keys(); 
				   
				   while(e_keys.hasMoreElements()) 
				   {  
					  String key=e_keys.nextElement().toString();
					  int tailValue=authorityHashtable.get(key);

					  if(UsefulMethods.getConsistencySystem().equals("linearizability") && tailValue==1)
					  {
						  System.out.println("At node "+nodeNumber+" is "+key+" "+hashtable.get(key)+" with authority "+authorityHashtable.get(key));
					  }	 
					  else if(UsefulMethods.getConsistencySystem().equals("eventual"))
					  {
						  System.out.println("At node "+nodeNumber+" is "+key+" "+hashtable.get(key)+" with authority "+authorityHashtable.get(key));
					  }
				   } 
				   
				   			   
				   if(startSearchingFromNode==next)
				   {
					   System.out.println("Everything is now printed");
				   }
				   else
				   {
					   sendMessageToNode(calculateSocketNumber(next),"queryAll "+startSearchingFromNode);
				   }				   
			   } 
			   else if(order.equals("deleteRecord")) //if we want to delete a record. 
			   {
				   int startSearchingFromNode=Integer.parseInt(parts[1]);
				   String hashArgumentString=parts[2];
				   BigInteger hashBigInteger=new BigInteger(parts[2],16);
				   String hashToFind=hashBigInteger.toString();
				   
				   if(hashtable.get(hashToFind)!=null && authorityHashtable.get(hashToFind)==replicationFactor)  //delete every replicated value.
				   {
					   System.out.println("At node "+nodeNumber+" value "+ hashtable.get(hashToFind)+" to be deleted");
					   hashtable.remove(hashToFind);
					   authorityHashtable.remove(hashToFind);
					   
					   if(UsefulMethods.getConsistencySystem().equals("eventual"))
					   {
						   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed");
					   }
					   
					   sendMessageToNode(calculateSocketNumber(next),"justRemoveRecord "+hashArgumentString+" "+(replicationFactor-1));
				   }
				   else
				   {
					   if(startSearchingFromNode==next)
					   {
						   System.out.println("No such element in DHT");
						   //tell the user to commit.
						   if(UsefulMethods.getConsistencySystem().equals("linearizability"))
						   {
							   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed");
						   }
						   if(UsefulMethods.getConsistencySystem().equals("eventual"))
						   {
							   sendMessageToNode(UsefulMethods.getMasterSocket(),"committed");
						   }
					   }
					   else //if we do not find the record, go on searching.
					   {
						   sendMessageToNode(calculateSocketNumber(next),"deleteRecord "+startSearchingFromNode+" "+hashArgumentString);
					   }
				   }
				   
			   }
			   else if(order.equals("justRemoveRecord"))
			   {
				   String hashArgumentString=parts[1];
				   BigInteger hashBigInteger=new BigInteger(hashArgumentString,16);
				   String hashToFind=hashBigInteger.toString();
				   int leftToReplicate=Integer.parseInt(parts[2]);
				   
				   if(UsefulMethods.getConsistencySystem().equals("linearizability") && leftToReplicate==0)
				   {
					    sendMessageToNode(UsefulMethods.getMasterSocket(),"committed");
				   }
				   
				   
				   if(leftToReplicate!=0)
				   {
					   System.out.println("At node "+nodeNumber+" value "+hashtable.get(hashToFind)+" to be deleted");
					   hashtable.remove(hashToFind);
					   authorityHashtable.remove(hashToFind);
					   sendMessageToNode(calculateSocketNumber(next),"justRemoveRecord "+hashArgumentString+" "+(leftToReplicate-1));
				   }
			   }
			   else if(order.equals("insertNode"))  //code to insert a new node.
			   {
				   int nodeToInsert=Integer.parseInt(parts[1]);
				   nodeCreator.createNode(nodeToInsert,replicationFactor);
				    
			   }
			   else if(order.equals("prepareToDeleteNode")) //code to order to delete a node.
			   {
				   argument1=Integer.parseInt(parts[1]);
				   int nextOfStartingNode=next;
				   
				   nodeCreator.deleteNode(argument1,nextOfStartingNode);			   
			   }
			   else if(order.equals("insertNodeReplicate")) //this and the next orders help us replicate when we insert a new node.
			   {
				   int nodeInserted=Integer.parseInt(parts[1]);
				   
				   sendMessageToNode(calculateSocketNumber(nodeInserted),"requestZoneTransfer");
			   }
			   else if(order.equals("requestZoneTransfer"))
			   {
				   sendMessageToNode(calculateSocketNumber(next),"sendBackRecordsStored");						
			   }
			   else if(order.equals("sendBackRecordsStored"))
			   {
				   
				   Enumeration<String> e_keys = hashtable.keys(); 
				   
				   while(e_keys.hasMoreElements()) 
				   {  
					  String key=e_keys.nextElement().toString();
					  int hashtableValue=hashtable.get(key);
					  int authorityHashtableValue=authorityHashtable.get(key);
					  BigInteger keyBigInteger=new BigInteger(key);
					  
					  if(keyBigInteger.compareTo(priorHash)<0)
					  {
						  sendMessageToNode(calculateSocketNumber(prior),"justInsert "+key+" "+hashtableValue+" "+authorityHashtableValue);
						  System.out.println("Value "+hashtable.get(key)+" with authority "+authorityHashtable.get(key)+" added at node "+nodeNumber+" moves to "+prior);
				   
					  
					  
						  if(authorityHashtableValue>1)
						  {
							  authorityHashtable.put(key, authorityHashtableValue-1);
							  System.out.println("NODE "+nodeNumber+": value "+hashtableValue+" with authority "+authorityHashtableValue+" changed to "+authorityHashtable.get(key));
						  }
						  else
						  {
							  System.out.println("NODE "+nodeNumber+" completely removed "+hashtable.get(key)+" with authority "+authorityHashtable.get(key));
							  hashtable.remove(key);
							  authorityHashtable.remove(key);
						  } 
					  }
				   }
				   
				   sendMessageToNode(calculateSocketNumber(next),"continueReplicateInsert "+(replicationFactor-1)+" "+priorHash);
			   }
			   else if(order.equals("continueReplicateInsert"))
			   {
				   int moreToGo=Integer.parseInt(parts[1]);
				   BigInteger hashToCompare=new BigInteger(parts[2]);
			   
				   if(moreToGo>0)
				   {
					   Enumeration<String> e_keys = hashtable.keys(); 
				   
					   while(e_keys.hasMoreElements()) 
					   {  
						   String key=e_keys.nextElement().toString();
						   int hashtableValue=hashtable.get(key);
						   int authorityHashtableValue=authorityHashtable.get(key);
						   BigInteger keyBigInteger=new BigInteger(key);
						   
						   if(keyBigInteger.compareTo(hashToCompare)<0)
						   {
							   if(authorityHashtableValue>1)
							   {
								   authorityHashtable.put(key, authorityHashtableValue-1);
								   System.out.println("NODE "+nodeNumber+": value "+hashtableValue+" with authority "+authorityHashtableValue+" changed to "+authorityHashtable.get(key));
							   }
							   else
							   {
								   System.out.println("NODE "+nodeNumber+" completely removed "+hashtable.get(key)+" with authority "+authorityHashtable.get(key));
								   hashtable.remove(key);
								   authorityHashtable.remove(key);
							   }  
						   }
					   }
					   
					   sendMessageToNode(calculateSocketNumber(next),"continueReplicateInsert "+(moreToGo-1)+" "+hashToCompare);  
				   }
			   }
			   else if(order.equals("justInsert"))
			   {
				   String key=parts[1];
				   int hashtableValue=Integer.parseInt(parts[2]);
				   int authorityHashtableValue=Integer.parseInt(parts[3]);
				   
				   hashtable.put(key, hashtableValue);
				   authorityHashtable.put(key,authorityHashtableValue);
				   System.out.println("Value "+hashtable.get(key)+" with authority "+authorityHashtable.get(key)+" added at node "+nodeNumber+" MOVED FROM "+next);
			   }
			 
				System.out.println("Looping at node "+nodeNumber+" with prior "+prior+" and next "+next+" priorHash "+priorHash+" and nextHash "+nextHash);
							
		   }		
			
		}
	    catch(Exception e)
    	{
	    	System.out.println(e);
	    }
	   
	   System.out.println("This is the end of the server socket");
	   System.out.println("DEATH to node "+nodeNumber);   
	   
	   Enumeration<String> e_keys = hashtable.keys(); 
	   
	   //if we delete a node, the records of the node should migrate to the next node and the authority values should change(head/tail).
	   while(e_keys.hasMoreElements()) 
	   {  
		  String key=e_keys.nextElement().toString();
		  int leftToReplicate=authorityHashtable.get(key);
		  int hashtableValue=hashtable.get(key);
		  int authoritative=prior;
		  
	   	  sendMessageToNode(calculateSocketNumber(next),"replicateInsertDelete "+key+" "+hashtableValue+" "+leftToReplicate+" "+authoritative);
	   } 

	   
	   try
	   {
		   Thread.sleep(2000);
		   serverSocket.close();
	   }
	   catch(Exception e)
	   {
		   System.out.println(e);
	   }
	 }
	
	//a method to help send messages to other nodes
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
			
			try
			{
				Thread.sleep(1);
				sendMessageToNode(socketNo,message);
			}
			catch(Exception e2)
			{
				System.out.println(e2);
			}
		//	System.out.println(e);
		}
	}
	
		
	public int calculateSocketNumber(int nodeNumber)
	{
		return (20000+nodeNumber);
	}
		
}