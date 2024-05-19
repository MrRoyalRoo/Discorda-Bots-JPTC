import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Quiz {
    private String theme;
    private boolean singlePlayer;
    private MessageChannelUnion channel;
    private User host;
    private List<User> players;
    private int questionAmount;
    private int id;

    public Quiz(String theme, boolean singlePlayer, User host, List<User> players, MessageChannelUnion channel, int questions, int id){
        this.host = host;
        this.singlePlayer = singlePlayer;
        this.theme = theme;
        this.players = players;
        this.channel = channel;
        this.questionAmount = questions;
        this.id = id;
    }

    public List<Question> getQuestions(String theme, int questions) throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource(theme+".json");
        File file_ = Paths.get(res.toURI()).toFile();
        String file = file_.getAbsolutePath();
        List<Question> questionList = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray jsonArray = new JSONArray(tokener);
            for (int j = 0; j < jsonArray.length(); j++){
                JSONObject json = jsonArray.getJSONObject(j);

                String question = json.getString("question");
                int answer = json.getInt("answer");

                List<String> choices = new ArrayList<>();

                JSONArray choicesJson = json.getJSONArray("choices");
                for (int i = 0; i < choicesJson.length(); i++) {
                    choices.add(choicesJson.getString(i));
                }
                int time = json.getInt("time");

                String explanation = json.getString("explanation");

                questionList.add(new Question(question, choices, answer, time, explanation));
            }

            while (questionList.size() > questions){
                Random random = new Random();
                questionList.remove(random.nextInt(questionList.size()));
            }

            return questionList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MessageChannelUnion getChannel(){
        return channel;
    }
    public String getTheme(){
        return theme;
    }
    public boolean isSinglePlayer(){
        return singlePlayer;
    }
    public User getHost(){
        return host;
    }
    public List<User> getPlayers(){
        return players;
    }
    public int getQuestionAmount(){
        return questionAmount;
    }
    public int getId(){
        return id;
    }
    public Quiz addPlayer(User user){
        players.add(user);
        return this;
    }


}
