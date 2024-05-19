import com.iwebpp.crypto.TweetNaclFast;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class StartQuiz extends ListenerAdapter {
    List<SelectOption> options = new ArrayList<>();
    HashMap<User, String> theme = new HashMap<>();
    HashMap<User, Boolean> singlePlayer = new HashMap<>();
    HashMap<User, Integer> questionCount = new HashMap<>();
    private static int currentQuizId = 0;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        super.onSlashCommandInteraction(e);
        if (e.getName().equals("startquiz")){
            if (e.getOption("question_amount").getAsInt() > 30){
                e.reply("The question amount is too big! Max questions are 30!").setEphemeral(true).queue();
            }else if(e.getOption("question_amount").getAsInt() < 5){
                e.reply("You can't start a quiz with a question amount of less than 5!").setEphemeral(true).queue();
            }else{
                questionCount.put(e.getUser(), e.getOption("question_amount").getAsInt());
                theme.put(e.getUser(), "math");
                if (e.getGuild() != null){
                    singlePlayer.put(e.getUser(), false);
                }else{
                    singlePlayer.put(e.getUser(), true);
                }

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Set the settings of your quiz!");
                embed.setColor(Color.GREEN);


                options.clear();
                options.add(SelectOption.of("Math", "math")
                        .withDefault(true)
                        .withDescription("Numbers...")
                        .withEmoji(Emoji.fromUnicode("\uD83D\uDD22")));
                options.add(SelectOption.of("Flags", "flags")
                        .withDescription("Country flags!")
                        .withEmoji(Emoji.fromUnicode("🚩")));
                options.add(SelectOption.of("Minecraft", "minecraft")
                        .withDescription("SPECIAL EDITION")
                        .withEmoji(Emoji.fromUnicode("\uD83C\uDFAE")));


                e.reply("")
                        .addEmbeds(embed.build())
                        .addActionRow(StringSelectMenu.create("themes").addOptions(options).build())
                        .addActionRow(Button.success("start", "Start"))
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent e) {
        super.onStringSelectInteraction(e);
        if (e.getComponentId().equals("themes")){
            e.deferEdit().queue();
            theme.replace(e.getUser(), e.getInteraction().getSelectedOptions().get(0).getValue());
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        super.onButtonInteraction(e);
        if (e.getButton().getId().equals("start")){
            e.deferEdit().queue();
            e.getMessage().delete().queue();
            Quiz quiz = new Quiz(theme.get(e.getUser()), singlePlayer.get(e.getUser()), e.getUser(), new ArrayList<>(), e.getChannel(), questionCount.get(e.getUser()), currentQuizId+1);
            currentQuizId++;
            try {
                QuizManager.start(quiz);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int handleQuizId(){
        currentQuizId++;
        return currentQuizId;
    }
}
