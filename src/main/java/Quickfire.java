import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class Quickfire extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        super.onSlashCommandInteraction(e);
        if (e.getName().equals("quickfire")){
            if (e.getGuild()==null){
                if (e.getOption("theme").getAsString().equals("math") || e.getOption("theme").getAsString().equals("flags") || e.getOption("theme").getAsString().equals("minecraft")){
                    e.reply("Started quickfire!").queue();
                    Quiz quiz = new Quiz(e.getOption("theme").getAsString(), true, e.getUser(), new ArrayList<>(), e.getChannel(), 1, StartQuiz.handleQuizId());
                    try {
                        QuizManager.start(quiz);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }else{
                    e.reply("The given theme does not exist!").setEphemeral(true).queue();
                }
            }else{
                e.reply("You can only start a quickfire in single player! (in private messages)").setEphemeral(true).queue();
            }
        }
    }
}
