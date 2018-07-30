package MyChordPackage;


public class MainClass {

	public static RequestResolver requestResolver;
	
	public static void main(String[] args)
	{
		int userWantsTo;
		requestResolver=new RequestResolver();
		
		//the DHT is initially empty
		UsefulMethods.setDhtEmptiness(0);

		//set replication rate
		UsefulMethods.requestReplicationRate();
		
		//set consistency system
		UsefulMethods.requestConsistencySystem();
	
		//interact with the user. Find what he wants to do
		while(true)
		{
			userWantsTo=requestResolver.interactWithUser();
			requestResolver.executeDependingOnOrder(userWantsTo);
		}
		
	}

}
