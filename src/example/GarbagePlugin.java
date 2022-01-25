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
            player.sendMessage("[scarlet]<[yellow]You[] -> [teal]" + other.coloredName() + "[scarlet]> [lightgrey]" + args[1]);
            other.sendMessage("[scarlet]<[teal]" + player.coloredName() + "[scarlet] -> [yellow]You[]> [lightgrey]" + args[1]);
        });
        
        handler.<Player>register("team", "<team> [player]", "Sets the team of yourself or another player.", (args, player) -> {
            Player other = player;
            if(args.size() == 2){
                Player other = Groups.player.find(p -> Strings.stripColors(p.name.replace(" ", "")).equalsIgnoreCase(args[1]));
            }
            if(other == null){
                player.sendMessage("[scarlet]Couldnt find the player \"" + args[1] + "\" (Did he leave?)");
                return;
            }
            try{
                int number = Integer.parseInt(args[0]);
                Team team = Team.get(number);
            }catch (NumberFormatException ex){
                player.sendMessage("[scarlet]" + args[1] + " is not a valid team, teams are any valid signed integer.\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[green]3 = Green\n[purple]4 = Purple\n[blue]5 = Blue");
                return;
            }
            if(other==player){
                player.sendMessage("[lightgrey]Set your team to team" + args[0]);
                return;
            }
            player.sendMessage("[lightgrey]Set " + other.coloredName() + "[lightgrey]'s team to team" + args[0]);
            other.sendMessage("[lightgrey]Your team was set to " + args[0] + " by " + player.coloredName() + "[lightgrey].");
        });
    }
}
