package com.scrom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import com.scrom.server.Player;


public class Lobby {

    /**
     * The port that the server listens on.
     */
    static ServerSocket listener;

    private static HashSet<Player> players = new HashSet<Player>();


    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */



    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        listener = new ServerSocket(60500);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            try {

                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);


                while (true) {
                    if( players.size()==5){
                        out.println("GameStarted");

                    }else{
                        out.println("SUBMITNAME");
                        name = in.readLine();
                        if (name == null) {
                            return;
                        }
                        synchronized (players) {
                            if (!containsPlayer(name)) {

                                players.add(new Player(name, out));
                                out.println("NAMEACCEPTED");
                                write("Welcome to: "+name);
                                //make a new game
                                if(players.size()==5){
                                    //make a new game
                                    System.out.println("gamestart");
                                }


                                break;
                            }
                        }
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            System.out.println(e.toString());
                        }
                    }}




                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    write(name + ": " + input);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e.toString());
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {

                if (name != null||out!=null) {
                    removePlayer(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void write(String msg){
        for (Player player : players) {
            player.writer.println("MESSAGE " + msg);
        }

    }
    private static void removePlayer(String name){
        for (Player player : players) {
            if(player.name==name){
                players.remove(player);
            }
        }

    }
    private static Boolean containsPlayer(String name){
        for (Player player : players) {
            if(player.name.equals(name)){
                return true;
            }
        }

        return false;

    }
}