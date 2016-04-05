/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thecherno.chernochat.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Алекс
 */
public class UniqueIndentifier {
    
    private static List<Integer> ids = new ArrayList<Integer>();
    private static final int  RANGE = 100;
    
    private static int index = 0;
    
    static{
    
        for(int i=0; i<RANGE;i++){
            ids.add(i);
        }
        Collections.shuffle(ids);
    }
    
    public static int getIndentifier(){
        if(index > ids.size() - 1)
        index = 0;
        return ids.get(index++);
    }
}
