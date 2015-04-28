##Sync
Very useful if you own more than one server and want to run a command to the other server without switching between servers.
The commands are run as console.

####Commands
######/sync list
shows a list with all servers name

######/sync all [command...]
execute the specified command on all servers

######/sync server [command...]
execute the specified command on the specified server

######/sync server1,server2,serverX [command...]
execute the specified command on the specified servers

######/sync reload
reloads the plugin (useful when you add another server)

####Permissions
sync.use - Permits all commands in all servers (default: op)
sync.reload - Reload the sync plugin (default: op)
sync.server.[serverName] - Permits to sync all commands to the specified server
sync.server.[serverName].[command] - Permits to sync the specified command to the specified server

####Configuration
Set a database MySQL and a server name on the config.yml file
