package DiscordBot.helpers;

public class BangHighScores {

    public double attemptCount;
    public String mostAttemptsPlayer;
    public double deathCount;
    public String mostDeathsPlayer;
    public double bestRate;
    public String luckiest;
    public double worstRate;
    public String unluckiest;
    public int jamCount;
    public String mostJamsPlayer;

    public BangHighScores(
            double attemptCount,
            String mostAttemptsPlayer,
            double deathCount,
            String mostDeathsPlayer,
            double bestRate,
            String luckiest,
            double worstRate,
            String unluckiest,
            int jamCount,
            String mostJamsPlayer){

        this.attemptCount = attemptCount;
        this.mostAttemptsPlayer = mostAttemptsPlayer;
        this.deathCount = deathCount;
        this.mostDeathsPlayer = mostDeathsPlayer;
        this.bestRate = bestRate;
        this.luckiest = luckiest;
        this.worstRate = worstRate;
        this.unluckiest = unluckiest;
        this.jamCount = jamCount;
        this.mostJamsPlayer = mostJamsPlayer;
    }
}
