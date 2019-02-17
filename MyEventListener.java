package DiscordBots;

import com.opencsv.CSVWriter;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MyEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        User author = event.getAuthor(); // author variable is the author of the message
        if (author.isBot()) return; // If the event is made by the bot, ignore it

        Message message = event.getMessage(); // Variable message is the detected message
        String content = message.getContentRaw(); // Variable content is the text of the message
        MessageChannel channel = event.getChannel(); // Variable channel is the text channel the message came from
        Guild guild = event.getGuild(); // Variable guild is the Discord server
        Member auth = guild.getMember(author); // Variable auth is of type Member for later use
        String path = "C:\\Users\\Shawn\\IdeaProjects\\Java Project\\src\\DiscordBots\\ElectiveRequests.csv"; // CSV file path


        // Bot helps with command usage
        if (content.toLowerCase().equals("!join") || content.toLowerCase().equals("!join ")){
            channel.sendMessage("Command: !join <courseID>\n\nExample: !join mcs2100").queue();
        }

        else if (content.toLowerCase().equals("!leave") || content.toLowerCase().equals("!leave ")){
            channel.sendMessage("Command: !leave <courseID>\n\nExample: !leave mcs2100").queue();
        }

        else if (content.toLowerCase().equals("!help")){
            channel.sendMessage("For instructions to join an elective group, say \"!join\" and to leave one, say \"!leave\"").queue();
        }

        // Bot responds with pong and latency
        else if (content.toLowerCase().equals("!ping")) {
            channel.sendMessage("Pong! " + event.getJDA().getPing() + " ms").queue();
        }

        // Bot creates new text channel and deletes old one (OWNER ONLY)
        else if (content.toLowerCase().equals("!totalchatwipe")) {
            if (auth.equals(guild.getOwner())) {
                event.getGuild().getTextChannelById(event.getChannel().getId()).createCopy().queue();
                event.getGuild().getTextChannelById(event.getChannel().getId()).delete().queue();
            }
            else{
                channel.sendMessage("You do not have permission to do that!").queue();
            }
        }

        // Bot gives requested role to target (MODERATOR->PEASANT ONLY)
        else if(content.startsWith("!giverole ")){
            content = content.substring(10,content.length());
            String roleName = content.substring(content.indexOf(" ")+1,content.length());
            Member member = message.getMentionedMembers().get(0);
            // If author is a moderator and target is not a moderator
            if (auth.getRoles().containsAll(guild.getRolesByName("Moderator",true))
                    && !member.getRoles().containsAll(guild.getRolesByName("Moderator", true))) {
                guild.getController().addRolesToMember(member, guild.getRolesByName(roleName, true)).queue();
            }
            else{
                channel.sendMessage("You do not have permission to do that!").queue();
            }
        }

        // Bot removes requested role from user (MODERATOR->PEASANT ONLY)
        else if(content.startsWith("!takerole ")) {
            content = content.substring(10, content.length());
            String roleName = content.substring(content.indexOf(" ") + 1, content.length());
            Member member = message.getMentionedMembers().get(0);
            // If author is a moderator and target is not a moderator
            if (auth.getRoles().containsAll(guild.getRolesByName("Moderator",true))
                    && !member.getRoles().containsAll(guild.getRolesByName("Moderator", true))) {
                guild.getController().removeRolesFromMember(member, guild.getRolesByName(roleName, true)).queue();
            }
            else{
                channel.sendMessage("You do not have permission to do that!").queue();
            }
        }

        // User requests to join/create an elective role (EVERYONE)
        else if(content.startsWith("!join ")){
            String roleName = content.substring(6, content.length());
            // If role is restricted, don't assign user to role
            if (roleName.toLowerCase().equals("moderator") || roleName.toLowerCase().contains("verified")){
                channel.sendMessage("I cannot set you to that role").queue();
                return;
            }
            // If role exists and isn't restricted, assign user to role
            if (!guild.getRolesByName(roleName,true).equals(guild.getRolesByName("lamptissueboxfritoscoke",true))) {
                guild.getController().addRolesToMember(auth, guild.getRolesByName(roleName, true)).queue();
                channel.sendMessage("Role \""+roleName+"\" added to "+auth.getAsMention()).queue();
            }
            else{ // If role does not exist
                File file = new File(path);
                try {
                    // Create writers, readers, threshold, etc
                    int threshold = 1; // Required number of applicants for new role
                    Boolean alreadyExists = false;
                    FileWriter fileWriter = new FileWriter(file, true);
                    CSVWriter csvWriter = new CSVWriter(fileWriter);
                    Path filePath = Paths.get(path);
                    BufferedReader reader = Files.newBufferedReader(filePath);
                    // If file is empty, give it appropriate headers
                    if (reader.readLine() == null){
                        String[] header = {"CourseID", "Applicant"};
                        csvWriter.writeNext(header);
                    }
                    // If user already requested this role, don't add this application to file
                    String line = reader.readLine();
                    while (line != null){
                        if (line.equals("\""+roleName+"\","+"\""+author.getId()+"\"")){
                            alreadyExists = true;
                        }
                        line = reader.readLine();
                    }
                    // If this is a new application, add it to file
                    if (!alreadyExists) {
                        String[] application = {roleName, author.getId()};
                        csvWriter.writeNext(application, true);
                    }
                    reader.close();
                    csvWriter.close();
                    fileWriter.close();
                    // Check how many people applied for the same role
                    String[] applicants = new String[threshold];
                    int applicationCount = 0;
                    BufferedReader reader2 = Files.newBufferedReader(filePath);
                    line = reader2.readLine();
                    while (line != null){
                        if (line.startsWith("\""+roleName+"\",")){
                            applicants[applicationCount] = line.substring(line.indexOf("\"",roleName.length()+3)+1,line.length()-1);
                            applicationCount++;
                        }
                        line = reader2.readLine();
                    }
                    reader2.close();
                    // If number of applications is sufficient, create role and channel for it, and assign all applicants to that role
                    if (applicationCount >= threshold && !alreadyExists){
                        Collection <Permission> viewChannel = new ArrayList<>(); // Permissions for that channel
                        ((ArrayList<Permission>) viewChannel).add(0,Permission.VIEW_CHANNEL);
                        guild.getController().createRole().setName(roleName).queue(); // Create the role
                        guild.getController().createTextChannel(roleName).setParent(guild.getCategoriesByName("Electives",true).get(0)).complete(); // Create the textChannel
                        TextChannel textChannel = guild.getTextChannelsByName(roleName,true).get(0); // Variable textChannel is the new channel
                        // Give role to all applicants
                        for (int i = 0; i < threshold; i++){
                            guild.getController().addRolesToMember(guild.getMemberById(applicants[i]),guild.getRolesByName(roleName,true)).queue();
                        }
                        // Prevent everyone from seeing the channel
                        textChannel.createPermissionOverride(guild.getRolesByName("@everyone",true).get(0)).setDeny(viewChannel).queue();
                        // Let people with the specified role see the channel
                        textChannel.createPermissionOverride(guild.getRolesByName(roleName,true).get(0)).setAllow(viewChannel).queue();
                        // Let moderators see the channel
                        textChannel.createPermissionOverride(guild.getRolesByName("Moderator",true).get(0)).setAllow(viewChannel).queue();
                        channel.sendMessage("The channel for your elective has been created! Only members of the channel can see it.").queue();
                    }
                    else{ // If number of applications is too low
                        if (alreadyExists){
                            channel.sendMessage("You already applied for this role!\nNumber of requests needed: "+ (threshold - applicationCount)).queue();
                        }
                        else{
                            channel.sendMessage("Role \"" + roleName + "\" does not exist, but the request has been noted.\nNumber of requests needed: " + (threshold - applicationCount)).queue();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Remove user's application from CSV file
        else if (content.toLowerCase().startsWith("!leave ")){
            String roleName = content.substring(7, content.length());
            try {
                Path filePath = Paths.get(path);
                // Get number of lines
                BufferedReader bufferedReader = Files.newBufferedReader(filePath);
                int lineCount = 0;
                int i = 0;
                String line;
                while(bufferedReader.readLine() != null){
                    lineCount++;
                }
                String[] fileContent = new String[lineCount];

                // Store file content in array
                BufferedReader reader = Files.newBufferedReader(filePath);
                while((line = reader.readLine()) != null){
                    fileContent[i] = line+"\n";
                    i++;
                }

                // Find application
                for (i = 0; i < lineCount; i++){
                    if (fileContent[i].equals("\""+roleName+"\","+"\""+author.getId()+"\"\n")){
                        fileContent[i] = "";
                    }
                }

                //Rewrite csv file
                File file = new File(path);
                FileWriter fileWriter = new FileWriter(file, false);
                CSVWriter csvWriter = new CSVWriter(fileWriter);
                for (i = 0; i < lineCount; i++) {
                    fileWriter.write(fileContent[i]);
                }
                csvWriter.close();
                fileWriter.close();

                // Remove role from user
                try {
                    guild.getController().removeSingleRoleFromMember(auth, guild.getRolesByName(roleName, true).get(0)).queue();
                    channel.sendMessage(auth.getAsMention()+" left "+roleName).queue();
                }catch(IndexOutOfBoundsException e){
                    channel.sendMessage("You do not have this role!").queue();
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // Delete all non-specified roles (OWNER ONLY)
        else if (content.toLowerCase().equals("!cleanroles")){
            if (auth.isOwner()){
                List <Role> listRoles = guild.getRoles();
                for (Role listRole : listRoles) { // Delete all roles that are not these
                    if (!(listRole.toString().toLowerCase().substring(2,listRole.toString().lastIndexOf("(")).equals("moderator") || listRole.toString().substring(2,listRole.toString().lastIndexOf("(")).equals("verified students") || listRole.toString().toLowerCase().substring(2,listRole.toString().lastIndexOf("(")).equals("@everyone") || listRole.toString().toLowerCase().substring(2,listRole.toString().lastIndexOf("(")).equals("discordbot"))) {
                        listRole.delete().queue();
                    }
                }
                channel.sendMessage("All non-essential roles deleted!").queue();
            }
            else{
                channel.sendMessage("You do not have permission to do that!").queue();
            }
        }

        // Delete all elective channels (OWNER ONLY)
        else if(content.toLowerCase().equals("!cleanelectives")){
            if (auth.isOwner()){
                List <Channel> listChannels = guild.getChannels();
                for (Channel listChannel : listChannels){
                    if (listChannel.getParent() != null && listChannel.getParent().equals(guild.getCategoriesByName("Electives",true).get(0))){
                        listChannel.delete().queue();
                    }
                }
                channel.sendMessage("All elective channels deleted!").queue();
            }
            else{
                channel.sendMessage("You do not have permission to do that!").queue();
            }
        }
    }
}
