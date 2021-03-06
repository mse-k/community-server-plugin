package garbo;

import arc.*;
import arc.util.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.world.blocks.storage.*;
import arc.struct.*;

public class GarbagePlugin extends Plugin{
    //argument handling, need to allow team names for team arg handler later
    private Team HandleTeamArg(String arg, Player player){
        try{
            int number = Integer.parseInt(arg);
            return Team.get(number);
        }catch (NumberFormatException ex){
            player.sendMessage("[scarlet]" + arg + " is not a valid team, teams are any valid signed integer." +
"\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[green]3 = Green\n[purple]4 = Purple\n[blue]5 = Blue");
            return null;
        }
    }
    private Player HandlePlayerArg(String arg, Player player){
        Player other = Groups.player.find(p -> Strings.stripColors(p.name.replaceAll("[^\\x21-\\x7E]", "")).equalsIgnoreCase(arg)); //remove all the shit
        if(other == null){
            if(arg.startsWith("id::")){
                try{
                    int number = Integer.parseInt(arg.substring(4));
                    Player other2 = Groups.player.find(p -> number == p.id);
                    if(other2 == null){
                        player.sendMessage("[scarlet]Couldnt find the player with id \"" + number + "\" (Did he leave?)");
                        return null;
                    }
                    return other2;
                }catch (NumberFormatException ex){
                };
            }
            player.sendMessage("[scarlet]Couldnt find the player \"" + arg + "\" (Did he leave?)");
            return null;
        }
        return other;
    }
    //stuff that does stuff
    private void KillAllUnits(){
        Groups.unit.each(u -> {
            if(!u.spawnedByCore){
                Time.run(Mathf.random(0f, 5f), () -> Call.unitDespawn(u));
            }
        });
    }
    private void KillAllUnits(Team team){
        team.data().units.each(u -> {
            if(!u.spawnedByCore){
                Time.run(Mathf.random(0f, 5f), () -> Call.unitDespawn(u));
            }
        });
    }
    private void KillAllBuilds(){
        Groups.build.each(b -> {
            if(!(b.block instanceof CoreBlock)){
                Time.run(Mathf.random(0f, 5f), () -> b.tile.setNet(Blocks.air));
            }
        });
    }
    private void KillAllBuilds(Team team, boolean cores){
        Seq<Building> builds = new Seq<>();
        if(team.data().buildings != null){
            team.data().buildings.getObjects(builds);
        }
        builds.each(b -> {
            if(b.team == team && (!(b.block instanceof CoreBlock) || cores)){
                Time.run(Mathf.random(0f, 5f), () -> b.tile.setNet(Blocks.air));
            }
        });
    }
    
    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("msg", "<player> <text...>", "Send a message only to another player.", (args, player) -> {
            Player other = HandlePlayerArg(args[0], player);
            if (other == null) return;
            player.sendMessage("[red]<[yellow]You[] -> [cyan]" + Strings.stripColors(other.name) + "[red]> [lightgrey]" + args[1]);
            other.sendMessage("[red]<[cyan]" + Strings.stripColors(player.name) + "[red] -> [yellow]You[]> [lightgrey]" + args[1]);
        });
        
        handler.<Player>register("team", "<team> [player]", "Sets the team of yourself or another player.", (args, player) -> {
            Player other = player;
            if(args.length == 2){
                other = HandlePlayerArg(args[1], player);
                if (other == null) return;
            }
            Team team = HandleTeamArg(args[0], player);
            if (team == null) return;
            other.team(team);
            if(other==player){
                player.sendMessage("[lightgrey]Set your team to team " + args[0]);
                return;
            }
            player.sendMessage("[lightgrey]Set " + other.name + "[lightgrey]'s team to team " + args[0]);
            other.sendMessage("[lightgrey]Your team was set to " + args[0] + " by " + player.name + "[lightgrey].");
        });
        
        handler.<Player>register("setteam", "<team> [player]", "Alt of /team for foos client users until its fixed", (args, player) -> {
            Player other = player;
            if(args.length == 2){
                other = HandlePlayerArg(args[1], player);
                if (other == null) return;
            }
            Team team = HandleTeamArg(args[0], player);
            if (team == null) return;
            other.team(team);
            if(other==player){
                player.sendMessage("[lightgrey]Set your team to team " + args[0]);
                return;
            }
            player.sendMessage("[lightgrey]Set " + other.name + "[lightgrey]'s team to team " + args[0]);
            other.sendMessage("[lightgrey]Your team was set to " + args[0] + " by " + player.name + "[lightgrey].");
        });
        
        handler.<Player>register("killall", "[team]", "Kills all units, optionally of just one team.", (args, player) -> {
            if(args.length == 1) {
                Team team = HandleTeamArg(args[0], player);
                if (team == null) return;
                Call.sendMessage("[lightgrey]All units on team " + args[0] + " have been killed by " + player.name + "[lightgrey].");
                KillAllUnits(team);
                return;
            }
            Call.sendMessage("[lightgrey]All units have been killed by " + player.name + "[lightgrey].");
            KillAllUnits();
        });
        
        handler.<Player>register("wipe", "[team] [cores]", "Removes all buildings, optionally of just one team. Can remove cores of a team too.", (args, player) -> {
            if(args.length > 0) {
                boolean cores = false;
                if(args.length == 2){
                    cores = args[1].equalsIgnoreCase("y") || args[1].equalsIgnoreCase("yes");
                }
                Team team = HandleTeamArg(args[0], player);
                if (team == null) return;
                if(team == Team.get(1) && cores){
                    player.sendMessage("[scarlet]Thats just /gameover with extra steps...");
                    return;
                }
                Call.sendMessage("[lightgrey]All builds on team " + args[0] + " have been wiped by " + player.name + "[lightgrey].");
                KillAllBuilds(team, cores);
                return;
            }
            Call.sendMessage("[lightgrey]All builds have been wiped by " + player.name + "[lightgrey].");
            KillAllBuilds();
        });
        
        handler.<Player>register("gameover", "Instantly triggers a game over. Cores are not killed.", (args, player) -> {
            Call.sendMessage(player.name + "[lightgrey] has caused a game over.");
            Events.fire(new GameOverEvent(Team.get(0)));
        });
        
        handler.<Player>register("changelog", "Checks the changelog of garbo plugin", (args, player) -> player.sendMessage("[purple]Garbo plugin[]\n[stat]Plugin by [#ff6000]mse\n\n[][][lightgrey]" +
//"[stat]v1.0.0:[]\nPlugin created\nAdded commands:\n/msg <user> <text...>\n/team <team> [player]\n\n" +
//"[stat]v1.0.1[]\nAdded commands:\n/killall [team]\n\n" +
"[stat]v1.0.2[]\nAdded commands:\n/wipe [team] [cores]\n/changelog\n\n" +
"[stat]v1.0.3[]\nAdded commands:\n/setteam <team> [player]\n/gameover\nBug fixes:\nRemoved the ability to wipe team sharded with cores enabled\n\n" +
"[stat]v1.1.0[]\nNew features:\nPlayer arguemnts not support ids through \"id::\"\nBug fixes:\n/killall and /wipe no longer locks up server with lots of stuff.\nFixed /killall and /wipe not removing all buildings/units for real this time. /wipe without a team still can not remove walls.\nFixed /gameover message not resetting color.\nMade all player arguments ignore special characters"));
    }
}
