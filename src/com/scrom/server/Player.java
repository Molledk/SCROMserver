package com.scrom.server;

import java.io.PrintWriter;
import java.util.HashSet;

public class Player {
    public String name;
    public PrintWriter writer;

    public Player(String n, PrintWriter w){
        this.name=n;
        this.writer=w;
    }

}
