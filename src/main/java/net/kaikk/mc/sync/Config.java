package net.kaikk.mc.sync;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Config {
	final static String configFilePath = "plugins" + File.separator + "Sync" + File.separator + "config.yml";
	private File configFile;
	FileConfiguration config;
	
	String serverName, dbUrl, dbUsername, dbPassword;
	List<String> staff;
	
	int ticks;
	
	Config() {
		this.configFile = new File(configFilePath);
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.load();
	}
	
	void load() {
		this.serverName=config.getString("serverName", "");
		
		this.dbUrl=config.getString("dbUrl", "jdbc:mysql://127.0.0.1/sync");
		this.dbUsername=config.getString("dbUsername", "sync");
		this.dbPassword=config.getString("dbPassword", "");
		this.ticks=config.getInt("ticks", 20);
		
		this.save();
	}
	
	void save() {
		try {
			this.config.set("serverName", this.serverName);
			this.config.set("dbUrl", this.dbUrl);
			this.config.set("dbUsername", this.dbUsername);
			this.config.set("dbPassword", this.dbPassword);
			this.config.set("ticks", this.ticks);
			
			this.config.save(this.configFile);
		} catch (IOException e) {
			Sync.instance.getLogger().severe("Couldn't save config file.");
			e.printStackTrace();
		}
	}
}