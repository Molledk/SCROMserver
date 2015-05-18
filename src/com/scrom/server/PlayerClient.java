package com.scrom.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

public class PlayerClient {
    public String name;
    public ObjectOutputStream writer;
    public ObjectInputStream reader;

    public PlayerClient(String n, ObjectOutputStream w, ObjectInputStream reader){
        this.name=n;
        this.writer=w;
        this.reader=reader;
    }

}
