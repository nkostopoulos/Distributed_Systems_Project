# Distributed_Systems_Project
Java Code for the Distributed Systems' project

The project requires the implementation of a distributed hashtable (DHT).   
   
Every node in the DHT acts either as a server or as a client.  
   
Every node has a hashed ID.  
   
A new item is hashed and added in the correct position in the hashtable.   

The DHT includes replication of content in k nodes.   
   
Possible actions:   
1) A new node can become member of the hashtable. The node should be placed in the correct position depending on its hash ID.     
2) A node can leave the hashtable. When that happens, we will have to replicate the contents of the node in the next node of the hashtable.   
3) Content can be inserted in the DHT. The content must be placed in the correctnode depending on its hash value as well as replicated.   
4) Content can be removed in the DHT. The content must be removed from all the nodes of the DHT.   
5) Somebody can query for content.   
   
Finally, consistency is implemented to make sure that nodes return the most fresh content.
