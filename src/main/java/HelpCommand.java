import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class HelpCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        super.onSlashCommandInteraction(e);
        if (e.getName().equals("help")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Help");
            embed.setColor(Color.RED);
            embed.addField(new MessageEmbed.Field("Starting the Quiz", "To start the quiz, type: /startquiz <the number of questions that you want(5-30)>.\n To start a single player quiz right click the bot and select 'Message'.\n If you want to play multiplayer just run the command in a server.", false));
            embed.addField(new MessageEmbed.Field("Quickfire", "To start a quickfire you need to be in single player (as described in the previous section).", false));
            e.reply("").addEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
