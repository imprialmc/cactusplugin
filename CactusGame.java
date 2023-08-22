package me.imprial.cactusgame;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public final class CactusGame extends JavaPlugin {

    //world copy methods...
    private static void copyFileStructure(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        if (!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void copyWorld(World originalWorld, String newWorldName) {
        copyFileStructure(originalWorld.getWorldFolder(), new File(Bukkit.getWorldContainer(), newWorldName));
        new WorldCreator(newWorldName).createWorld();
    }
    public static boolean unloadWorld(World world) {
        return world!=null && Bukkit.getServer().unloadWorld(world, false);
    }
    public static void deleteWorld(String worldName){
        //find world folder and unload, then deletes it
        File folder = Bukkit.getWorld(worldName).getWorldFolder();
        unloadWorld(Bukkit.getWorld(worldName));
        folder.delete();
    }


    //game code
    @Override
    public void onEnable() {
        // Plugin startup logic

        //Register commands...
        getCommand("cactus");
        //Load the template world
        loadWorld("map");

    }


    //Cactus game


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            Player p = (Player) sender;
            if(command.getName().equalsIgnoreCase("cactus")){ //cactus duels command
                p.sendMessage("You ran the cactus command");
                if(args.length != 0){
                    //if the arg is duel, run this
                    if(args[0].equals("duel")){
                        p.sendMessage("HELLO THERE");
                        //check if the player is online. if not, tell them they are offline
                        if(Bukkit.getPlayerExact(args[1]) != null){
                            DuelPlayer( p, Bukkit.getPlayerExact(args[1]) );
                        } else if(p == Bukkit.getPlayerExact(args[1])){
                               p.sendMessage(ChatColor.RED + "you cant duel yourself lol");
                        } else{
                            p.sendMessage(ChatColor.RED + "this guy isnt on your tripping lol");
                        }

                    }


                }
            }
        }

        return true;
    }


    //set up the players map template
    private void loadWorld(String worldName) {
        //attempts to get the world
        World world = Bukkit.getWorld(worldName);
        //if the world returns null, it will load up the map, if there is no map it will create it, which is nono >:(
        if (world == null) {
            WorldCreator worldCreator = new WorldCreator(worldName);
            world = worldCreator.createWorld();
        }

        if (world != null) {
            getLogger().info("World '" + worldName + "' loaded.");
        } else {
            getLogger().warning("Failed to load world '" + worldName + "'.");
        }
    }



    //Runs a 1v1 against two players
    void DuelPlayer(Player player1, Player player2){
        player1.sendMessage("You did the method!");
        //create a world for the players to play on
        String worldName = player1.getDisplayName() + "-" + player2.getDisplayName() + "-map";
        copyWorld(Bukkit.getWorld("map"), worldName );
        //create a map reference after loading the map in
        loadWorld(worldName);
        World gameMap = getServer().getWorld(worldName);
        //set the spawn points for the players
        Location spawn1 = new Location(gameMap, 0.5, 82, -6.5);
        Location spawn2 = new Location(gameMap, 0.5, 82, 6.5);
        Location hub = new Location(Bukkit.getWorld("world"), 0 ,0,0);

        //Send the players to thier spawn point
        BukkitTask task = new CactusGame().tpPlayers(player1, player2).runTaskLater(this, 20); //make dealyed tp :)
        player1.teleport(spawn1);
        player2.teleport(spawn2);

        //set their healths to be half heart
        player1.setMaxHealth(0.5);
        player1.setHealth(0.5);

        player2.setMaxHealth(0.5);
        player2.setHealth(0.5);

        //give them their items


        //start game loop
        //if players die, game stops running
        boolean gameRunning = true;
        //timer system
        long startingTime = System.currentTimeMillis();
        while(gameRunning){
            long timeElapsed = System.currentTimeMillis() - startingTime;

            if(player1.getHealth() > 0 || player2.getHealth() > 0) {
                gameRunning = true;

            } else {
                player1.teleport(hub);
                player2.teleport(hub);

                player1.sendMessage("good game");
                player2.sendMessage("good game");
                deleteWorld(worldName);
                gameRunning = false;
            }
        }

    }
    public void giveEgg(Player p1, Player p2) {
        // What you want to schedule goes here
        p1.sendMessage("you get egg");
        p2.sendMessage("you get egg");
    }
    public void tpPlayers(Player p1, Player p2){

    }

}
