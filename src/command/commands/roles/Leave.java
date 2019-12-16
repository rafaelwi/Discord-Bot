package command.commands.roles;

import command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Leave extends Command {

    /**
     * @see Command
     * Initializes the command's key to "!leave".
     */
    public Leave() {
        super("!leave", false);
    }

    /**
     * @see Join
     * Allows user to leave a role.
     *
     * @param event the MessageReceivedEvent that triggered the command
     */
    @Override
    public void start(MessageReceivedEvent event) {

    }
}
