/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thecherno.chernochat.server;

import java.net.InetAddress;

/**
 *
 * @author Алекс
 */
public class ServerClient {
    
    public String name;
    public InetAddress address;
    public int port;
    private final int ID;
    public int attempt = 0;
    
    public ServerClient(String name,InetAddress address, int port, final int ID){
        this.ID = ID;
        this.name = name;
        this.address = address;
        this.port = port;
    }
    
    public int getID(){
        return ID;
    }
    
}
