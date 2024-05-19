import net.dv8tion.jda.api.entities.User;

import java.util.*;

public class Leaderboard {

    private HashMap<User, Long> scores;

    public Leaderboard(){
        scores = new HashMap<>();
    }

    public String getLeaderboard(){
        List<Map.Entry<User, Long>> sortedList = new ArrayList<>(scores.entrySet());
        StringBuilder leaderboard = new StringBuilder();
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        for (int i = 0; i < sortedList.size(); i++){
            int place = i+1;
            leaderboard.append("#").append(place).append(". ").append(sortedList.get(i).getKey().getName()).append(" - **").append(sortedList.get(i).getValue()).append("**\n");
        }
        return leaderboard.toString();
    }

    public void increaseScore(User user, long score){
        scores.replace(user, score+scores.get(user));
    }

    public void addPlayer(User user){
        scores.put(user, 0L);
    }

    public Long getScore(User user){return scores.get(user);}

}
