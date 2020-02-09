package command;

import command.commands.admin.Cleanup;
import command.commands.bang.Bang;
import command.commands.bang.BangScore;
import command.commands.bang.MyBang;
import command.commands.blackjack.Bet;
import command.commands.blackjack.Hit;
import command.commands.blackjack.MyHand;
import command.commands.blackjack.Stand;
import command.commands.economy.Buy;
import command.commands.Help;
import command.commands.economy.Gift;
import command.commands.economy.Market;
import command.commands.admin.Purge;
import command.commands.economy.Wallet;
import command.commands.misc.Flip;
import command.commands.misc.Id;
import command.commands.misc.Info;
import command.commands.misc.Karma;
import command.commands.misc.Ping;
import command.commands.roles.Join;
import command.commands.roles.Leave;
import command.commands.roles.RemoveRole;
import command.commands.roles.Roles;
import command.commands.bang.Daily;

import java.util.ArrayList;

/**
 * Container class for a list of all the commands
 * available in the server.
 */
public final class CommandList {

    private static ArrayList<Command> commands;

    private CommandList() {

    }

    static {
        commands = new ArrayList<>();
        addAllCommands();
    }

    /**
     * Commands list getter.
     *
     * @return the ArrayList of all commands
     */
    public static ArrayList<Command> getCommands() {
        return commands;
    }

    /**
     * Populates the commands list with every command.
     */
    private static void addAllCommands() {
        // Admin commands
        commands.add(new Purge());
        commands.add(new Cleanup());

        // Everyone commands
        commands.add(new Bang());
        commands.add(new BangScore());
        commands.add(new Bet());
        commands.add(new Buy());
        commands.add(new Daily());
        commands.add(new Flip());
        commands.add(new Help());
        commands.add(new Hit());
        commands.add(new Id());
        commands.add(new Info());
        commands.add(new Join());
        commands.add(new Karma());
        commands.add(new Leave());
        commands.add(new Market());
        commands.add(new MyBang());
        commands.add(new MyHand());
        commands.add(new Ping());
        commands.add(new Roles());
        commands.add(new Stand());
        commands.add(new Wallet());
        commands.add(new Gift());
        commands.add(new RemoveRole());
    }
}