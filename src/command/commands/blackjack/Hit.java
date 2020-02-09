package command.commands.blackjack;

import command.Command;
import command.util.cards.HandOfCards;
import command.util.cards.PhotoCombine;
import command.util.game.BlackJackGame;
import command.util.game.BlackJackList;
import database.connectors.EconomyConnector;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.FileInputStream;

public class Hit extends Command {

    private EconomyConnector ec;

    /**
     * Initializes the command's key to "!hit".
     */
    public Hit() {
        super("!hit", false);
        ec = new EconomyConnector();
    }

    /**
     * Every command must be able to be activated based on the event.
     *
     * @param event the MessageReceivedEvent that triggered the command
     */
    @Override
    public void start(MessageReceivedEvent event) {
        BlackJackGame game = BlackJackList.getUserGame(event.getAuthor().getIdLong());

        if (game == null) {
            event.getChannel().sendMessage("You haven't started a game yet!\n"
                    + "To start a new one, say `!bet <amount>`").queue();
            return;
        }

        try {
            File file = new File(".");
            String path = file.getAbsolutePath().replace("build/libs/.", "");

            String output = "";
            int value = game.hit();

            if (PhotoCombine.genPhoto(game.getPlayer().getHand().getAsList())) {
                event.getChannel().sendMessage(event.getAuthor().getName() + "'s hand is now: "
                        + game.getPlayer().getHand().toString())
                        .addFile(new FileInputStream(path + "res/out.png"), "out.png").queue();
            } else {
                event.getChannel().sendMessage(event.getAuthor().getName() + "'s hand is now: "
                        + game.getPlayer().getHand().toString()).queue();
            }

            if (value >= 21) {
                int reward = game.checkWinner();
                HandOfCards dealerHand = game.getDealer().getHand();
                output += value == 21 ? "You got 21!\n" : "You busted.\n";

                if (game.getDealer().getHand().getValue() > 21)
                    output += "Dealer busted!\n";
                if (value > 21 && dealerHand.getValue() > 21 || value == dealerHand.getValue())
                    output += "Tie game, you didn't win or lose any money.";
                else
                    output += (reward >= 0 ? "You earned " + reward : "You lost " + (-reward)) + " *gc*";

                ec.addOrRemoveMoney(event.getAuthor().getIdLong(), reward);

                output += "\nDealers hand: " + game.getDealer().getHand().toString();
                if (PhotoCombine.genPhoto(game.getDealer().getHand().getAsList())) {
                    event.getChannel().sendMessage(output)
                            .addFile(new FileInputStream(path + "res/out.png"), "out.png").queue();
                } else {
                    event.getChannel().sendMessage(output).queue();
                }

                BlackJackList.removeGame(game);
            }
        } catch (Exception e) {
            printStackTraceAndSendMessage(event, e);
        }
    }
}
