import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {
    public static void main(String[] args) {
        String token = "MTIzMTU5OTUzODI0NzMwNzM3Ng.G1FJGv.vMpXK8d3q59qn-vfKJp63XYfY_leTGLNxaXzwQ";
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new HelpCommand(), new StartQuiz(), new QuizManager(), new Quickfire())
                .setActivity(Activity.customStatus("The trainer who trains"))
                .build();

        jda.upsertCommand("startquiz", "Start the quiz!").addOption(OptionType.INTEGER, "question_amount", "The number of quiz questions!", true, false).queue();
        jda.upsertCommand("help", "Get help!").queue();
        jda.upsertCommand("quickfire", "Get one question!").addOption(OptionType.STRING, "theme", "Select the theme", true, false).queue();
    }
}
