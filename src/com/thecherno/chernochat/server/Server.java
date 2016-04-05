package com.thecherno.chernochat.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

        private List<ServerClient> clients = new ArrayList();
        private List<Integer> clientResponse = new ArrayList<Integer>();
    
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
        
        private final int MAX_ATTEMPT = 5;
        private boolean showlog = false;

	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		run.start();
	}

	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
                
                Scanner scanner = new Scanner(System.in);
                while(running){
                    String text = scanner.nextLine();
                    
                    if(text.startsWith("/")){
                        if(text.equalsIgnoreCase("/showlog")){
                            showlog = true;
                        }else if(text.equalsIgnoreCase("/hidelog")){
                            showlog = false;
                        }else if(text.equalsIgnoreCase("/clients")){
                            System.out.println("=============Connected clients===============");
                            for(int i = 0; i < clients.size(); i++){
                                ServerClient c = clients.get(i);
                                System.out.println("Name: " + c.name + " address" + c.address + " id client: " + c.getID());
                            }
                             System.out.println("=============Connected clients===============");
                        }else if(text.split(" ")[0].equalsIgnoreCase("/kick")){
                            kickingUser(text.split(" ")[1]);
                        }else{
                            System.out.println("terminal doesnot find those command");
                        }
                        }else{
                            System.out.println("You put command without bash symbol");
                        }
                }
	}

	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
                                    sendToAll("/i/server");
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    for(int i=0; i < clients.size(); i++){
                                        ServerClient c = clients.get(i);
                                        if(!clientResponse.contains(c.getID())){
                                            if(c.attempt >= MAX_ATTEMPT){
                                                disconnect(c.getID(), false);
                                            }else{
                                                c.attempt++;
                                            }
                                        }else{
                                            clientResponse.remove(new Integer(c.getID()));
                                            c.attempt = 0;
                                        }
                                    }
				}
			}
		};
		manage.start();
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
                                        process(packet);
                                    
				}
			}
		};
		receive.start();
	}
        
        private void sendToAll(String message){
            if (message.startsWith("/m/")) {
			String text = message.substring(3);
			text = text.split("/e/")[0];
			//System.out.println(message);
		}
            
            for(int i=0; i<clients.size();i++){
                ServerClient client = clients.get(i);
                send(message.getBytes(), client.address, client.port);   
            }
            
        }
        
        private void send(final byte[] data, InetAddress address, final int port){
            send = new Thread("Send"){
                public void run(){
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    try {
                        socket.send(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            send.start();
        }
        
        private void send(String message, InetAddress address, int port){
            message += "/e/";
            send(message.getBytes(), address, port);
        }
        
        private void process(DatagramPacket packet){
            String string = new String(packet.getData());
            if(string.startsWith("/c/")){
                int id = UniqueIndentifier.getIndentifier();
                System.out.println("Indentifier is " + id);
                clients.add(new ServerClient(string.split("/c/|/e/")[1], packet.getAddress(), 
                                                                packet.getPort(), id));
                System.out.println("connected client: " + string.split("/c/|/e/")[1]);
                String ID = "/c/" + id;
                send(ID, packet.getAddress(), packet.getPort());
               
            }else if(string.startsWith("/m/")){
                if(showlog){
                    System.out.println(string.substring(0,string.lastIndexOf("/e/") + 1));
                }
                sendToAll(string);
            }else if(string.startsWith("/d/")){
                String id = string.split("/d/|/e/")[1];
                disconnect(Integer.parseInt(id), true);
            }else if(string.startsWith("/i/")){
                if(showlog){
                    System.out.println(string.substring(0,string.lastIndexOf("/e/") + 1));
                }
                clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
            }else{
            
            }
            
        }
        
        private void disconnect(int id, boolean status){
            boolean existed = false;
            ServerClient serveclient = null;
            for(int i =0; i < clients.size(); i++){
                if(clients.get(i).getID() == id){
                    serveclient = clients.get(i);
                    clients.remove(i);
                    existed = true;
                    break;
                }
            }
            String message = "";
            if(!existed) return;
            if(status){
                message = "Client " + " disconnected " + serveclient.name;
            }else{
                 message = "Client " + serveclient.name + "(" + serveclient.getID() + ") time out";
            }
            System.out.println(message);
            
        }

    private void kickingUser(String string) {
        try{
            int userId = Integer.parseInt(string);
            kickingUser(userId);
        }catch(NumberFormatException ex){
            kickingUserbyName(string);
        }
    }
    
    private void kickingUserbyName(String name){
        boolean exist = false;
        for(int i = 0; i < clients.size(); i++){
                ServerClient c = clients.get(i);
                if(c.name.equals(name)){
                    disconnect(c.getID(), true);
                    exist = true;
                    break;
                }        
        }
        if(!exist) System.out.println("User doesnot found");
        
    }
    
    private void kickingUser(int userId){
        for(int i = 0; i < clients.size(); i++){
                ServerClient c = clients.get(i);
                if(c.getID() == userId){
                    disconnect(c.getID(), true);
                }        
            }
    }

}
