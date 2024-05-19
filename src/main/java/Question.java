import java.util.List;

public class Question {

    private String question;
    private List<String> choices;
    private int answer;
    private int time;
    private String explanation;

    public Question(String question, List<String> choices, int answer, int time, String explanation){
        this.answer = answer;
        this.question = question;
        this.choices = choices;
        this.time = time;
        this.explanation = explanation;
    }

    public String getQuestion(){
        return question;
    }

    public List<String> getChoices(){
        return choices;
    }

    public int getAnswer(){
        return answer;
    }

    public int getTime(){return time;}

    public String getExplanation(){return explanation;}

}
