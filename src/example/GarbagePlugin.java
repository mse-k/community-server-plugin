package garbo;

import arc.*;
import arc.util.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.*;
import arc.struct.*;
import java.text.*;

public class GarbagePlugin extends Plugin{
    private static final Seq<String> teamNames = new Seq<String>(new String[]{"derelict", "sharded", "crux", "malis", "green", "blue"});
    //argument handling, need to allow team names for team arg handler later
    private Team HandleTeamArg(String arg, Player player){
        try{
            int number = Integer.parseInt(arg);
            return Team.get(number);
        }catch (NumberFormatException ex){
            int index = teamNames.indexOf(arg);
            if (index == -1) {
                player.sendMessage("[scarlet]" + arg + " is not a valid team, teams are any valid signed integer or the name of a team." +
"\n[grey]0 = Derelict\n[yellow]1 = Sharded\n[red]2 = Crux\n[purple]3 = Malis\n[green]4 = Green\n[blue]5 = Blue");
                return null;
            }
            return Team.get(index);
        }
    }
    private Player HandlePlayerArg(String arg, Player player){
        Player other = Groups.player.find(p -> Strings.stripColors(Normalizer.normalize(p.name, Normalizer.Form.NFKD).replaceAll("[^\\x21-\\x7E]", "")).equalsIgnoreCase(Normalizer.normalize(arg, Normalizer.Form.NFKD).replaceAll("[^\\x21-\\x7E]", ""))); //remove all the shit
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
    private float[] HandlePosArg(String arg, Player player){
        if (arg == "pos") {
            Unit unit = player.unit();
            if (unit == null) {
                player.sendMessage("[scarlet]Cant use pos if you arent in a unit");
                return null;
            }
            return new float[]{unit.x, unit.y};
        }
        if (arg == "cur") {
            player.sendMessage("[scarlet]Not implemented :hehehehehaw:");
            return null;
        }
        String[] split = arg.split(",", 0);
        if (split.length == 2) {
            try{
                float x = Float.parseFloat(split[0]);
                float y = Float.parseFloat(split[1]);
                return new float[]{x, y};
            }catch (NumberFormatException ex){}
        }
        player.sendMessage("[scarlet]" + arg + " is not a valid position, positions are formatted like this: \n172,66\n92.7,-85" +
"\nyou can also use \"pos\" to use the position of your unit or \"cur\" for your cursor position (not implemented)." + arg.length());
        return null;
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
        team.data().buildings.each(b -> {
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
                player.sendMessage("[lightgrey]Set your team to " + team.toString().replace("#", " "));
                return;
            }
            player.sendMessage("[lightgrey]Set " + other.name + "[lightgrey]'s team to " + team.toString().replace("#", " "));
            other.sendMessage("[lightgrey]Your team was set to " + team.toString().replace("#", " ") + " by " + player.name + "[lightgrey].");
        });
        
        handler.<Player>register("killall", "[team]", "Kills all units, optionally of just one team.", (args, player) -> {
            if(args.length == 1) {
                Team team = HandleTeamArg(args[0], player);
                if (team == null) return;
                Call.sendMessage("[lightgrey]All units on team " + team.toString().replace("team#", "") + " have been killed by " + player.name + "[lightgrey].");
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
                if(!player.admin && team == Vars.state.rules.defaultTeam && cores && !Vars.state.rules.pvp){
                    player.sendMessage("[scarlet]guh..");
                    return;
                }
                Call.sendMessage("[lightgrey]All builds on team " + team.toString().replace("team#", "") + " have been wiped by " + player.name + "[lightgrey].");
                KillAllBuilds(team, cores);
                return;
            }
            Call.sendMessage("[lightgrey]All builds have been wiped by " + player.name + "[lightgrey].");
            KillAllBuilds();
        });
        
        handler.<Player>register("core", "<pos> [team] [tier]", "Places a core on your or the specified team. Destroys anything under the spot.", (args, player) -> {
            Team team = player.team();
            float[] pos = HandlePosArg(args[0], player);
            if (pos == null) return;
            int x = (int) pos[0];
            int y = (int) pos[1];
            Block type = Blocks.coreShard;
            if(args.length > 1) {
                team = HandleTeamArg(args[1], player);
                if (team == null) return;
                if(args.length > 2) {
                    switch(args[2]) {
                        case "1":
                            type = Blocks.coreShard;
                            break;
                        case "2":
                            type = Blocks.coreFoundation;
                            break;
                        case "3":
                            type = Blocks.coreNucleus;
                            break;
                        case "4":
                            type = Blocks.coreBastion;
                            break;
                        case "5":
                            type = Blocks.coreCitadel;
                            break;
                        case "6":
                            type = Blocks.coreAcropolis;
                            break;
                        default:
                            player.sendMessage("[scarlet]" + args[2] + " is not a valid core tier; valid tiers are numbers from 1 to 6" +
"\n[lightgrey]1 = Core: Shard\n2 = Core: Foundation\n3 = Core: Nucleus\n4 = Core: Bastion\n5 = Core: Citadel\n6 = Core: Acropolis");
                            return;
                    }
                }
            }
            Tile tile = Vars.world.tile(x, y);
            if (tile == null) {
                player.sendMessage("[scarlet]Position is outside of the map bounds.");
                return;
            }
            tile.setNet(type, team, 0);
            player.sendMessage("[lightgrey]Placed " + type.name.replace("-", " ") + " at " + x + ", " + y + ".");
        });
        
        handler.<Player>register("gameover", "Instantly triggers a game over. Cores are not killed.", (args, player) -> {
            if (!player.admin) {
                player.sendMessage("[scarlet]guh..");
                return;
            }
            Call.sendMessage("[lightgrey]" + player.name + "[lightgrey] has caused a game over.");
            Events.fire(new GameOverEvent(Team.get(0)));
        });
        
        /*handler.<Player>register("changelog", "Checks the changelog of garbo plugin", (args, player) -> player.sendMessage("[purple]Garbo plugin[]\n[stat]Plugin by [#ff6000]mse\n\n[][][lightgrey]" +
//"[stat]v1.0.0:[]\nPlugin created\nAdded commands:\n/msg <user> <text...>\n/team <team> [player]\n\n" +
//"[stat]v1.0.1[]\nAdded commands:\n/killall [team]\n\n" +
//"[stat]v1.0.2[]\nAdded commands:\n/wipe [team] [cores]\n/changelog\n\n" +
//"[stat]v1.0.3[]\nAdded commands:\n/setteam <team> [player]\n/gameover\nBug fixes:\nRemoved the ability to wipe team sharded with cores enabled\n\n" +
"[stat]v1.1.0[]\nNew features:\nPlayer arguments now support ids through \"id::\"\nBug fixes:\n/killall and /wipe no longer locks up server with lots of stuff.\nFixed /killall and /wipe not removing all buildings/units for real this time. /wipe without a team still can not remove walls.\nFixed /gameover message not resetting color.\nMade all player arguments ignore special characters" +
"[stat]v1.1.1[]\nUpdated to v138\nSome small changes\n\n" +
"[stat]v1.1.2[]\nUpdated to v140\nAdded commands:\n/core <pos> [team] [tier]\nNew features:\nSupport for team names in commands that require a team\nChanged name character filtering, should be better now\nBug fixes:\nFixed name color in /gameover\n\n"));*/
    }
}
