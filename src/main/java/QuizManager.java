import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class QuizManager extends ListenerAdapter {
    private static HashMap<User, Question> currentQuestions = new HashMap<>();
    private static HashMap<User, Quiz> activeUsers = new HashMap<>();
    private static HashMap<User, String> answers = new HashMap<>();
    private static HashMap<User, Long> answerTime = new HashMap<>();
    private static HashMap<User, Long> questionStarted = new HashMap<>();
    private static HashMap<Integer, Quiz> quizzes = new HashMap<>();
    private static HashMap<Quiz, Leaderboard> leaderboards = new HashMap<>();


    public static void start(Quiz quiz) throws InterruptedException, URISyntaxException {
        leaderboards.put(quiz, new Leaderboard());
        activeUsers.put(quiz.getHost(), quiz);
        quizzes.put(quiz.getId(), quiz.addPlayer(quiz.getHost()));
        if (!quiz.isSinglePlayer()){
            Timer timer = new Timer();
            Long time = System.currentTimeMillis() / 1000L + 30;

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Press the join button to join this quiz!");
            embed.setColor(Color.GREEN);
            embed.addField(new MessageEmbed.Field("Theme: **"+quiz.getTheme()+"**", "The joining will close <t:"+time+":R>.", true));
            embed.addField(new MessageEmbed.Field("Question amount: **"+quiz.getQuestionAmount()+"**", "", true));

            quiz.getChannel().sendMessage("").addEmbeds(embed.build()).addActionRow(Button.success("join_quiz_" + quiz.getId(), "Join")).queue(
                    message -> {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                message.delete().queue();
                            }
                        }, 30000);
                    }
            );
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        playQuiz(quiz);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }, 30000);
        }else{
            playQuiz(quiz);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        super.onButtonInteraction(e);
        if (e.getButton().getId().startsWith("quiz_")) {
            String button = e.getButton().getId().split("_")[1];
            Quiz quiz = activeUsers.get(e.getUser());
            if (quiz != null) {
                if (!(quiz.getId() == Integer.parseInt(e.getButton().getId().split("_")[2]))) {
                    e.reply("You can't interact in other quizzes!").setEphemeral(true).queue();
                } else {
                    e.deferEdit().queue();
                    answers.put(e.getUser(), button);
                    answerTime.put(e.getUser(), System.currentTimeMillis());
                }
            } else {
                e.reply("You need to join a quiz or start one to answer questions!").setEphemeral(true).queue();
            }
        }else if(e.getButton().getId().startsWith("join_quiz_")){
            e.deferEdit().queue();
            int quizId = Integer.parseInt(e.getButton().getId().split("_")[2]);
            activeUsers.put(e.getUser(), quizzes.get(quizId));
            Quiz quiz = quizzes.get(quizId).addPlayer(e.getUser());
            quizzes.replace(quizId, quiz);
        }
    }

    private static long calculateScore(long started, long answered){
        long time = answered-started;
        float scoreFraction = 1 - (time / 10000F);
        return Math.round(scoreFraction * 500);
    }

    private static void playQuiz(Quiz quiz) throws URISyntaxException {
        Leaderboard leaderboard = leaderboards.get(quiz);
        for (User player : quiz.getPlayers()){
            leaderboard.addPlayer(player);
        }
        Timer timer = new Timer();
        List<Question> questions = quiz.getQuestions(quiz.getTheme(), quiz.getQuestionAmount());
        int seconds = 0;
        for (Question question : questions) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    for (User player : quiz.getPlayers()) {
                        currentQuestions.put(player, question);
                    }
                    List<Button> buttons = new ArrayList<>();
                    for (int j = 0; j < question.getChoices().size(); j++) {
                        buttons.add(Button.primary("quiz_" + question.getChoices().get(j).toLowerCase() + "_" + quiz.getId() + "_" + System.currentTimeMillis(), question.getChoices().get(j)));
                    }
                    for (User player : quiz.getPlayers()) {
                        questionStarted.put(player, System.currentTimeMillis());
                    }
                    Long time = System.currentTimeMillis() / 1000L + question.getTime();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle(question.getQuestion());
                    embed.setColor(Color.GREEN);
                    embed.addField(new MessageEmbed.Field("", "This question will close <t:"+time+":R>.", true));

                    quiz.getChannel().sendMessage("").addEmbeds(embed.build()).addActionRow(buttons).queue(message -> {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                List<Button> buttons2 = new ArrayList<>();
                                for (Button button : buttons) {
                                    if (button.getId().split("_")[1].equalsIgnoreCase(question.getChoices().get(question.getAnswer()))) {
                                        Button newButton = Button.success(button.getId(), button.getLabel());
                                        buttons2.add(newButton.asDisabled());
                                    } else {
                                        Button newButton = Button.danger(button.getId(), button.getLabel());
                                        buttons2.add(newButton.asDisabled());
                                    }
                                }

                                EmbedBuilder new_embed = new EmbedBuilder();
                                new_embed.setTitle(question.getQuestion());
                                new_embed.setColor(Color.GREEN);
                                new_embed.addField(new MessageEmbed.Field("", "This question has closed.", true));

                                MessageEditBuilder msg = new MessageEditBuilder();
                                msg.setContent("");
                                msg.setEmbeds(new_embed.build());

                                message.editMessage(msg.build()).queue();
                                message.editMessageComponents(ActionRow.of(buttons2)).queue();
                                for (User player : quiz.getPlayers()) {
                                    if (question.getChoices().get(question.getAnswer()).equalsIgnoreCase(answers.get(player)) && answerTime.get(player) != null) {
                                        leaderboard.increaseScore(player, 500 + calculateScore(questionStarted.get(player), answerTime.get(player)));
                                        answerTime.remove(player);
                                        answers.remove(player);
                                    }
                                }
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        EmbedBuilder explanation = new EmbedBuilder();
                                        explanation.setTitle("Explanation");
                                        explanation.addField("", question.getExplanation(), true);
                                        quiz.getChannel().sendMessage("").addEmbeds(explanation.build()).queue();
                                        if (quiz.isSinglePlayer()){
                                            EmbedBuilder embed = new EmbedBuilder();
                                            embed.setColor(Color.YELLOW);
                                            if (questions.indexOf(question) == questions.size()-1){
                                                embed.setTitle("You finished with **" + leaderboard.getScore(quiz.getHost()) + "** points!");
                                                quiz.getChannel().sendMessage("").addEmbeds(embed.build()).queue();
                                            }else{
                                                embed.setTitle("You now have **" + leaderboard.getScore(quiz.getHost()) + "** points!");
                                                quiz.getChannel().sendMessage("").addEmbeds(embed.build()).queue();
                                            }
                                        }else{
                                            EmbedBuilder embed = new EmbedBuilder();
                                            embed.setColor(Color.YELLOW);
                                            embed.addField(new MessageEmbed.Field(leaderboard.getLeaderboard(), "", true));
                                            if (questions.indexOf(question) == questions.size()-1){
                                                embed.setTitle("The quiz ended and this is the final leaderboard!");
                                                quiz.getChannel().sendMessage("").addEmbeds(embed.build()).queue();
                                            }else{
                                                embed.setTitle("Leaderboard!");

                                                quiz.getChannel().sendMessage("").addEmbeds(embed.build()).queue();
                                            }
                                        }
                                    }
                                }, 3000);
                            }
                        }, question.getTime() * 1000L);
                    });
                }
            }, seconds * 1000L);
            seconds += question.getTime() + 10;
        }
    }
}
