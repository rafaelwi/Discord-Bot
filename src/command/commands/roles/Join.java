package command.commands.roles;

import command.Command;
import database.connectors.RolesConnector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Join extends Command {

    private RolesConnector rs;
    private MessageReceivedEvent theEvent;
    private String courseId;

    /**
     * @see Command
     * Initializes the command's key to "!join".
     */
    public Join() {
        super("!join", false);
        rs = new RolesConnector();
    }

    /**
     * Compares a string to the command's key and checks if that
     * string starts with the key.
     *
     * @param string the user's input being compared to the key
     * @return returns true if the key matches and false otherwise
     */
    @Override
    public boolean keyMatches(String string) {
        return string.toLowerCase().startsWith(getKey());
    }

    /**
     * @see Leave
     * Allows user to join a role.
     *
     * @param event the MessageReceivedEvent that triggered the command
     */
    @Override
    public void start(MessageReceivedEvent event) {
        if (event.getMember() == null)
            return;

        String message = event.getMessage().getContentRaw().toLowerCase();
        String[] strings = message.split(" ");
        MessageChannel channel = event.getChannel();
        theEvent = event;

        if (strings.length != 2) {
            channel.sendMessage("Command: !join <courseID>\n`Example: !join mcs2100`").queue();
            return;
        }

        courseId = message.substring(message.indexOf(strings[1]));
        if (!event.getGuild().getRolesByName(courseId, true).isEmpty()) {
            attemptRoleAssignment();
            return;
        }

        if (!courseExists()) {
            channel.sendMessage("There is no course with that ID").queue();
            return;
        }

        applyForRole();
    }

    /**
     * Tries to assign a role based on courseId to a member.
     */
    private void attemptRoleAssignment() {
        Guild guild = theEvent.getGuild();
        Category category = guild.getCategoryById("556266020625711130");

        if (category == null)
            return;

        MessageChannel channel = theEvent.getChannel();
        Role role = guild.getRolesByName(courseId, true).get(0);
        List<TextChannel> channels = guild.getTextChannelsByName(courseId, true);
        List<TextChannel> electiveChannels = category.getTextChannels();

        if (Objects.requireNonNull(guild.getMember(theEvent.getAuthor())).getRoles().contains(role))
            channel.sendMessage("You already have this role!").complete();

        else if (channels.isEmpty() || !electiveChannels.contains(channels.get(0)))
            channel.sendMessage("I cannot set you to that role").complete();

        else {
            guild.addRoleToMember(Objects.requireNonNull(theEvent.getMember()), role).queue();
            channel.sendMessage("Role " + courseId + " added to "
                    + Objects.requireNonNull(theEvent.getMember()).getAsMention()).complete();
        }
    }

    /**
     * Tries to apply for a role that doesn't yet exist.
     */
    private void applyForRole() {
        MessageChannel channel = theEvent.getChannel();
        try {
            if (rs.userAppliedForRole(courseId, theEvent.getAuthor().getIdLong())) {
                channel.sendMessage("You already applied for that role").queue();
                return;
            }

            if (rs.getNumApplications(courseId) < 3) {
                rs.applyForRole(courseId, theEvent.getAuthor().getIdLong());
                channel.sendMessage("Added your application to " + courseId + "!").queue();
                return;
            }

            // Create role/channel and apply them to applicants
            createRole();
            giveRoleToApplicants();

            channel.sendMessage("The channel you applied for was created! "
                    + "Only members of the channel can see it.").queue();
        } catch (Exception e) {
            printStackTraceAndSendMessage(theEvent, e);
        }
    }

    /**
     * Searches the file for the course.
     *
     * @return true if course is found in the file
     */
    private boolean courseExists() {
        String tsv = new File("").getAbsolutePath();
        tsv = tsv.replace("build/libs", "") + "res/courses.tsv";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tsv));
            String line, temp, tempId;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                temp = line.split("\t")[0].split(" ")[0].replace("*", "").toLowerCase();
                tempId = courseId.replaceAll("\\*", "").toLowerCase();

                if (temp.equals(tempId)) {
                    return true;
                }
            }
        } catch (IOException ignored) { }
        return false;
    }

    /**
     * Creates a role and text channel with specific permissions
     * such that only people with the new role can see the new
     * channel.
     */
    private void createRole() {
        Guild guild = theEvent.getGuild();

        // Create role and channel
        guild.createRole().setName(courseId).queue();
        guild.createTextChannel(courseId).setParent(guild.getCategoriesByName("Electives", true).get(0)).queue(); // might be complete()
        TextChannel textChannel = guild.getTextChannelsByName(courseId, true).get(0);
        Role role = guild.getRolesByName(courseId, true).get(0);

        // Set new channel permissions
        if (textChannel.getPermissionOverride(role) == null)
            textChannel.createPermissionOverride(role).queue(); // might be complete()

        // Let people with the specified role see the channel and read/send messages
        Objects.requireNonNull(textChannel.getPermissionOverride(role)).getManager().grant(Permission.VIEW_CHANNEL).queue();
        Objects.requireNonNull(textChannel.getPermissionOverride(role)).getManager().grant(Permission.MESSAGE_READ).queue();

        // Prevent everyone from seeing the channel
        if (textChannel.getPermissionOverride(guild.getRolesByName("@everyone", true).get(0)) == null)
            textChannel.createPermissionOverride(guild.getRolesByName("@everyone", true).get(0)).queue(); // might be complete()

        Objects.requireNonNull(textChannel.getPermissionOverride(guild.getRolesByName("@everyone", true).get(0))).getManager().deny(Permission.MESSAGE_READ).queue();

        // Do not let people with this role do @everyone or change nicknames
        Objects.requireNonNull(textChannel.getPermissionOverride(role)).getManager().deny(Permission.MESSAGE_MENTION_EVERYONE).queue();
        Objects.requireNonNull(textChannel.getPermissionOverride(role)).getManager().deny(Permission.NICKNAME_CHANGE).queue();
    }

    /**
     * Gives a newly created role to all people who applied for it.
     *
     * @throws SQLException may be thrown when interacting with the database
     */
    private void giveRoleToApplicants() throws SQLException {
        ArrayList<Long> applicants = rs.getApplicantIds(courseId);
        Role role = theEvent.getGuild().getRolesByName(courseId, true).get(0);

        for (int i = 0; i < 4; i++) {
            theEvent.getGuild().addRoleToMember(Objects.requireNonNull(theEvent.getGuild().getMemberById(applicants.get(i))), role).queue();
        }
        theEvent.getGuild().addRoleToMember(Objects.requireNonNull(theEvent.getMember()), role).queue();
    }
}
