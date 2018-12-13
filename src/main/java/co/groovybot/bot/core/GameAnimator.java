package co.groovybot.bot.core;

import co.groovybot.bot.GroovyBot;
import co.groovybot.bot.core.audio.LavalinkManager;
import co.groovybot.bot.util.NameThreadFactory;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameAnimator implements Runnable {

    private final ShardManager shardManager;
    private final GroovyBot groovyBot;
    private Game[] games;

    public GameAnimator(GroovyBot groovyBot) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NameThreadFactory("GameAnimator"));
        List<Game> gameList = new ArrayList<>();
        this.groovyBot = groovyBot;
        this.shardManager = groovyBot.getShardManager();
        groovyBot.getConfig().getJSONArray("games").forEach(game -> gameList.add(parseGame(String.valueOf(game))));
        this.games = gameList.toArray(new Game[0]);
        shardManager.setStatus(OnlineStatus.ONLINE);
        scheduler.scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        shardManager.setGame(games[ThreadLocalRandom.current().nextInt(games.length)]);
    }

    private Game parseGame(String game) {
        String preparesString = parsePlaceholders(game);
        if (game.startsWith("p: "))
            return Game.playing(preparesString.replaceFirst("p: ", ""));
        else if (game.startsWith("l: "))
            return Game.listening(preparesString.replaceFirst("l: ", ""));
        else if (game.startsWith("s: "))
            return Game.streaming(preparesString.replaceFirst("s: ", ""), "https://twitch.tv/groovydevs");
        else if (game.startsWith("w: "))
            return Game.watching(preparesString.replaceFirst("w: ", ""));
        return null;
    }

    private String parsePlaceholders(String game) {
        return game.replace("%channels%", String.valueOf(LavalinkManager.countPlayers())).replace("%guilds%", String.valueOf(shardManager.getGuilds().size())).replace("%users%", String.valueOf(shardManager.getUsers().size())).replace("%shards%", String.valueOf(shardManager.getShardsTotal())).replace("%prefix%", groovyBot.getConfig().getJSONObject("settings").getString("prefix")).replace("%name%", groovyBot.getShardManager().getApplicationInfo().getJDA().getSelfUser().getName());
    }
}