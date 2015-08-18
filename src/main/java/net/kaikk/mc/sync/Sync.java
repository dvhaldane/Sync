package net.kaikk.mc.sync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Sync extends JavaPlugin {
	static Sync instance;
	Config config;
	DataStore ds;

	public void onEnable() {
		instance=this;
		this.config=new Config();
		
		if (this.config.serverName.isEmpty() || this.config.serverName.matches("^.*[^a-zA-Z0-9].*$") || this.config.serverName.equalsIgnoreCase("all")) {
			this.getLogger().severe("Sync requires an unique server name in config.yml. Server name must be alphanumeric.");
			return;
		}
		
		try {
			this.ds=new DataStore(this, this.config.dbUrl, this.config.dbUsername, this.config.dbPassword);
			
			new QueryTask().runTaskLaterAsynchronously(this, 20L);
			this.getLogger().info("Sync loaded.");
		} catch (Exception e) {
			e.printStackTrace();
			instance=null;
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("sync")) {
			if (args.length==0) {
				sender.sendMessage("Usage: /sync [srvname|all] [command]");
				return false;
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("sync.reload")) {
					sender.sendMessage("You don't have permission to execute this command.");
					return false;
				}
				sender.sendMessage("Reloading Sync.");
				this.onEnable();
				return true;
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				sender.sendMessage("Servers name list:");
				for (String tableName : this.ds.allServersTableName) {
					sender.sendMessage("- "+tableName);
				}
				return true;
			}
			
			if (args.length<2) {
				sender.sendMessage("Usage: /sync [srvname|all] [command]");
				return false;
			}
			
			if (!sender.hasPermission("sync.use") && !sender.hasPermission("sync.server."+args[0]) && !sender.hasPermission("sync.server."+args[0]+"."+args[1])) {
				sender.sendMessage("You don't have permission to execute this command.");
				return false;
			}
			
			if (args.length==3 && args[1].equalsIgnoreCase("sync") && !(args[2].equalsIgnoreCase("reload") || args[2].equalsIgnoreCase("list"))) {
				sender.sendMessage("You can't sync a sync command!");
				return false;
			}
			
			String command=mergeStringArrayFromIndex(args, 1);
			
			if (args[0].equalsIgnoreCase("all")) {
				for (String tableName : this.ds.allServersTableName) {
					this.ds.dbQueue.add("INSERT INTO sync_srv_"+tableName+" VALUES(\""+command.replace("\"", "\\\"")+"\")");
				}
				sender.sendMessage("Sync will execute the command to all servers.");
				this.getServer().getLogger().info(sender.getName()+" run sync "+args[0]+" "+command);
				return true;
			}

			String[] servers = args[0].split(",");
			
			for (String tableName : servers) {
				if (this.ds.allServersTableName.contains(tableName)) {
					this.ds.dbQueue.add("INSERT INTO sync_srv_"+tableName+" VALUES(\""+command.replace("\"", "\\\"")+"\")");
				} else {
					sender.sendMessage("Server "+tableName+" doesn't exist.");
				}
			}
			sender.sendMessage("Sync will execute the command to specified servers.");
			this.getServer().getLogger().info(sender.getName()+" run sync "+args[0]+" "+command);
			return true;
		}
		
		return false;
	}
	
	// API
	public static boolean sync(String serverName, String command) {
		if (!instance.ds.allServersTableName.contains(serverName)) {
			return false;
		}
		instance.ds.dbQueue.add("INSERT INTO sync_srv_"+serverName+" VALUES(\""+command.replace("\"", "\\\"")+"\")");
		return true;
	}
	
	public static String[] servers() {
		return instance.ds.allServersTableName.toArray(new String[instance.ds.allServersTableName.size()]);
	}
	
	static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		StringBuilder sb = new StringBuilder();
		
		for(;i<arrayString.length;i++){
			sb.append(arrayString[i]);
			sb.append(' ');
		}
		
		if (sb.length()!=0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}
