package garbo;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.entities.*;

public class GarbagePlugin extends Plugin{
    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        
        handler.<Player>register("msg", "<player> <text...>", "Send a message only to another player.", (args, player) -> {
            Player other = Groups.player.find(p -> Strings.stripColors(p.name.replace(" ", "")).equalsIgnoreCase(args[0]));
            if(other == null){
                player.sendMessage("[scarlet]Couldnt find the player \"" + args[0] + "\" (Did he leave?)");
                return;
            }
            player.sendMessage("[red]<[yellow]You[] -> [cyan]" + Strings.stripColors(other.name) + "[red]> [lightgrey]" + args[1]);
            other.sendMessage("[red]<[cyan]" + Strings.stripColors(player.name) + "[red] -> [yellow]You[]> [lightgrey]" + args[1]);
        });
        
        handler.<Player>register("team", "<team> [player]", "Sets the team of yourself or another player.", (args, player) -> {
            Player other = player;
            if(args.length == 2){
                other = Groups.player.find(p -> Strings.stripColors(p.name.replace(" ", "")).equalsIgnoreCase(args[1]));
            }
            if(other == null){
                player.sendMessage("[scarlet]Couldnt find the player \"" + args[1] + "\" (Did he leave?)");
                return;
            }
            Team team = Team.get(0);
            try{
                int number = Integer.parseInt(args[0]);
                team = Team.get(number);
            }catch (NumberFormatException ex){
                player.sendMessage("[scarlet]" + args[0] + " is not a valid team, teams are any valid signed integer.\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[green]3 = Green\n[purple]4 = Purple\n[blue]5 = Blue");
                return;
            }
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
                Team team = Team.get(0);
                try{
                    int number = Integer.parseInt(args[0]);
                    team = Team.get(number);
                }catch (NumberFormatException ex){
                    player.sendMessage("[scarlet]" + args[0] + " is not a valid team, teams are any valid signed integer.\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[green]3 = Green\n[purple]4 = Purple\n[blue]5 = Blue");
                    return;
                }
                Call.sendMessage("[lightgrey]All units on team " + args[0] + " have been killed by " + player.name + "[lightgrey].");
                for(Unit u:Groups.unit){
                    if(u.team == team && !u.spawnedByCore){
                        Call.unitDespawn(u);
                    }
                }
                return;
            }
            Call.sendMessage("[lightgrey]All units have been killed by " + player.name + "[lightgrey].");
            for(Unit u:Groups.unit){
                if(!u.spawnedByCore){
                    Call.unitDespawn(u);
                }
            }
        });
        
        handler.<Player>register("wipe", "[team] [cores]", "Removes all buildings, optionally of just one team. Can remove cores and units of a team too.", (args, player) -> {
            if(args.length == 1) {
                Team team = Team.get(0);
                try{
                    int number = Integer.parseInt(args[0]);
                    team = Team.get(number);
                }catch (NumberFormatException ex){
                    player.sendMessage("[scarlet]" + args[0] + " is not a valid team, teams are any valid signed integer.\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[green]3 = Green\n[purple]4 = Purple\n[blue]5 = Blue");
                    return;
                }
                Call.sendMessage("[lightgrey]All units on team " + args[0] + " have been killed by " + player.name + "[lightgrey].");
                for(Unit u:Groups.unit){
                    if(u.team == team && !u.spawnedByCore){
                        Call.unitDespawn(u);
                    }
                }
                return;
            }
            Call.sendMessage("[lightgrey]All units have been killed by " + player.name + "[lightgrey].");
            for(Unit u:Groups.unit){
                if(!u.spawnedByCore){
                    Call.unitDespawn(u);
                }
            }
        });
        
        handler.<Player>register("changelog", "Checks changelog of the plugin", (args, player) -> {
            player.sendMessage("[purple]Garbo plugin[]\n[stat]Plugin by [#ff6000]mse\n[][]\n\n[lightgrey][stat]v1.0:[]\nPlugin created\nAdded commands:\n/msg <user> <text...>\n/team <team>\n\n[stat]v1.0.1[]\nMade");
        });
    }
}
