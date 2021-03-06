package edu.cmu.ece845.loadbalancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @Author: Yifan Li
 * @Description: 
 * This class will be responsible to handle the node joining and build connections between nodes and the server.
 * This class will maintain the status table of all the nodes in the system from the heart beat messages.
 * Whenever there is some change of states in the system, the manager will send the notifications
 * to the nodes. 
 * @Date: April 12, 2015
 */
public class NodeHiringServer implements Runnable{
	
	public ConcurrentHashMap<Integer,Boolean> nodeStatusMap;
	public ConcurrentHashMap<Integer,Socket> nodeSocMap; // we assume the socket for each node won't change
	public ConcurrentHashMap<Integer, Integer> nodeComPortMap; // the port which the node is using to listening to other nodes
	public ConcurrentHashMap<Integer, NodeListener> nodeListenerMap;
	
	public LoadBalancerMain lb;
	public int masterID;
	public int nodeCount;
	public int portNum;
	private volatile boolean running;
	
	public NodeHiringServer(int port, LoadBalancerMain lb){
		this.portNum = port;
		this.lb = lb;
		running = true;
		nodeCount = 0;
		masterID = 0; 
		this.nodeStatusMap = new ConcurrentHashMap<Integer, Boolean>();
		this.nodeSocMap = new ConcurrentHashMap<Integer, Socket>();
		this.nodeComPortMap = new ConcurrentHashMap<Integer, Integer>();
		this.nodeListenerMap = new ConcurrentHashMap<Integer, NodeListener>();
	}
	
	@Override
	public void run() {
		System.out.println("NodeHiringServer starts to listen!");
		ServerSocket serverSoc;
		try {
			serverSoc = new ServerSocket(portNum);
			
			while(running){
				try {
					Socket nodeSocket = serverSoc.accept();
					nodeJoin(nodeSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
	
	private void nodeJoin(Socket soc){
		//check whether this is a recovery join or not.
		//now I assume every node is newbie
		System.out.println("node:"+soc.getInetAddress()+
				":" + soc.getPort() + " has joined!");
		nodeSocMap.put(nodeCount, soc);
		nodeStatusMap.put(nodeCount, true);
		
		//start the nodeListener thread
		try {
			NodeListener nl = new NodeListener(this,nodeCount, soc);
			this.nodeListenerMap.put(nl.getNodeID(), nl);
			Thread t1 = new Thread(nl);
			t1.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		nodeCount++;
	}
	
	public int getMasterID(){
		//return the node who is alive and has the min ID
		return 0;
	}

}
