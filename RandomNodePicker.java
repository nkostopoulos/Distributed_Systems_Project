//The purpose of this class is to pick a random node from those active
//This random node is the node from whom insert, query and delete will start processing
//NONE OF THE NODES IS PERMITTED TO ACCESS THE ARRAYLIST TO CHECK FOR OTHER NODES. This would violate the idea of a P2P system.

package MyChordPackage;

import java.util.ArrayList;
import java.util.Random;

public class RandomNodePicker {
	
	//an ArrayList to keep the nodes active in DHT
	public static ArrayList<Integer> arrayList=new ArrayList<Integer>();
	
	//a method to add a node to the ArrayList
	public static void addNodeToList(int nodeNumber)
	{
		arrayList.add(nodeNumber);
		return ;
	}
	
	//a method to remove a node from the ArrayList
	public static void removeNodeFromList(int nodeNumber)
	{
		int index=arrayList.indexOf(nodeNumber);
		
		if(index!=-1)
		{
			arrayList.remove(index);
		}
		
		return ;
	}
	
	//a method to pick a random node from the ArrayList
	public static int pickRandomNumber()
	{
		int sizeOfList=arrayList.size();
		
		Random randomGenerator=new Random();
		int randomInt=randomGenerator.nextInt(sizeOfList);
		
		return arrayList.get(randomInt);
	}
}
