package com.scrom.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.scrom.model.SCROM;


public class Lobby {

    /**
     * The port that the server listens on.
     */
    static ServerSocket listener;

    private static HashSet<PlayerClient> players = new HashSet<PlayerClient>();



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
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
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
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                /*in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);*/


                while (true) {
                    if( players.size()==5){
                        oos.write(("GameStarted").getBytes());

                    }else{
                        oos.write(("NAME").getBytes());
                        byte[] inName = new byte[256];
                        name = inName.toString();
                        if (name == null) {
                            return;
                        }
                        synchronized (players) {
                            if (!containsPlayer(name)) {

                                players.add(new PlayerClient(name, oos));

                                //make a new game
                                if(players.size()==5){
                                    System.out.println("gamestart");
                                    //make a new game
                                    SCROM game = new SCROM();
                                    for (PlayerClient pl:players) game.addPlayer(pl.name);
                                    game.pregame();
                                    write(game);
                                    //game is on
                                    while(true){

                                        game.preturn();


                                    }

                                }


                                break;
                            }
                        }


                    }}




                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }

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

    private static void write(SCROM game){
        for (PlayerClient player : players) {
            player.writer.write(game);
        }

    }
    private static void removePlayer(String name){
        for (PlayerClient player : players) {
            if(player.name==name){
                players.remove(player);
            }
        }

    }
    private static Boolean containsPlayer(String name){
        for (PlayerClient player : players) {
            if(player.name.equals(name)){
                return true;
            }
        }

        return false;

    }
}