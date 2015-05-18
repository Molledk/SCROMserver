package com.scrom.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.scrom.model.Player;
import com.scrom.model.SCROM;
import com.scrom.model.action.PlayerAction;
import com.scrom.model.action.ScromAction;
import com.scrom.model.action.ServerAction;
import com.sun.corba.se.spi.activation.Server;
import sun.nio.ch.ThreadPool;


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

                            if (!containsPlayer(name)) {

                                players.add(new PlayerClient(name, oos, ois));

                                //make a new game
                                if(players.size()==5){
                                    System.out.println("gamestart");
                                    //make a new game
                                    SCROM game = new SCROM();
                                    for (PlayerClient pl:players) game.addPlayer(pl.name);
                                    game.pregame();
                                    //initialize the players version of the game
                                    write(new ServerAction(ServerAction.ActionType.Initialize,null,game));
                                    //game is on
                                    while(true){
                                        ServerAction NewTurn = new ServerAction(ServerAction.ActionType.NewTurn,null,null);
                                        //perform logic for a new turn and broadcast that youve done so
                                        performAndWrite(game, NewTurn);
                                        ServerAction DealCard = new ServerAction(ServerAction.ActionType.CardPlayed,game.getCurrent().getID(),game.getCurrentCard());
                                        //broadcast the current card and the current player
                                        write(DealCard);
                                        Thread[] rec = new Thread[players.size()];
                                        int iter = 0;
                                        for(PlayerClient pc: players){
                                            rec[++iter] = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    boolean ready = false;
                                                    while(!ready){
                                                        PlayerAction action = null;
                                                        try {
                                                            action = (PlayerAction)pc.reader.readObject();
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        } catch (ClassNotFoundException e) {
                                                            e.printStackTrace();
                                                        }
                                                        switch (action.getActionType()){
                                                            case Ready:
                                                                ready = true;
                                                                break;
                                                            default:
                                                                performAndWrite(game,action);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        for(int i = 0; i < rec.length; i++){
                                            rec[i].start();
                                        }
                                        for(int i = 0; i < rec.length; i++){
                                            rec[i].join();
                                        }
                                        resolveRound(game);
                                    }

                                }


                                break;
                            }
                        }


                    }




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
    private static void resolveRound(SCROM game) {
        ServerAction resolveAction = new ServerAction(ServerAction.ActionType.Resolve,game.getCurrent().getID(),game.getCurrentCard());
        performAndWrite(game,resolveAction);
    }

    private static void performAndWrite(SCROM game, ScromAction newTurn) {
        perform(game, newTurn);
        write(newTurn);
    }

    private static void write(ScromAction action){
        for (PlayerClient player : players) {
            try {
                player.writer.writeObject(action);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private static void perform(SCROM game, ScromAction action){
        if(action instanceof ServerAction){
            ServerAction a = (ServerAction)action;
            switch(a.getActionType()){
                case NewTurn:
                    game.preturn();
                    break;
                case Resolve:
                    game.resolve();
                    break;

            }
        }else if(action instanceof PlayerAction){
            PlayerAction a = (PlayerAction)action;
            switch(a.getActionType()){

            }

        }
    }
}