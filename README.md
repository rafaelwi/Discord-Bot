Version 1.9.1
# Purpose
Manage Computer Science Discord elective roles and channels and entertain users with games

# Commands
### Anyone
`!help` - displays other commands that can be used

`!ping` - returns "pong!" and your latency in ms. Keeps track of user's best/worst scores

`!bang` - play Russian roulette

`!mybang` - shows player's bang scores

`!bangscore` or `!bangscores` - shows high scores for attempts, deaths, survival rate, and death rate

`!roles` - shows a list of available roles to join

`!join [role name]` - if the role already exists, it is assigned to the user. Else, the user's id and requested role are stored in a database. If enough users apply for the same role, the role and a private text channel are created and all applicants are assigned to it

`!leave [role name]` - unassigns role from user and removes their application for that role from the database

### Moderator+
`!giverole [mentioned user] [role name]` - if the target is not a moderator, the role is assigned to the target  

`!takerole [mentioned user] [role name]` - same as `!giverole`, but removes the role

### Owner only
`!totalchatwipe` - clones current text channel and deletes the original (effectively wipes chat history)

`!cleanelectives` - deletes all text channels in the Electives category

`!cleanroles` - deletes all roles except Moderator and Verified Students
