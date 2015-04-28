package net.kaikk.mc.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

class DataStore {
	private Sync instance;
	private String dbUrl;
	private String username;
	private String password;
	protected Connection db = null;
	
	ArrayList<String> allServersTableName = new ArrayList<String>();
	ConcurrentLinkedQueue<String> dbQueue = new ConcurrentLinkedQueue<String>();

	DataStore(Sync instance, String url, String username, String password) throws Exception {
		this.instance=instance;
		this.dbUrl = url;
		this.username = username;
		this.password = password;
		
		try {
			//load the java driver for mySQL
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to load Java's mySQL database driver.  Check to make sure you've installed it properly.");
			throw e;
		}
		
		try {
			this.dbCheck();
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to connect to database.  Check your config file settings. Details: \n"+e.getMessage());
			throw e;
		}

		try {
			Statement statement = db.createStatement();

			// Creates tables on the database		
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS sync_srv_"+Sync.instance.config.serverName+" (command char(100) NOT NULL) ENGINE MEMORY;");
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to create the necessary database table. Details: \n"+e.getMessage());
			throw e;
		}
		
		try {
			Statement statement = db.createStatement();

			// Creates tables on the database		
			ResultSet result = statement.executeQuery("SHOW TABLES LIKE 'sync_srv_%';");
			while(result.next()) {
				this.allServersTableName.add(result.getString(1).substring(9));
			}
		} catch(Exception e) {
			this.instance.getLogger().severe("Couldn't retrieve all servers table name.");
			throw e;
		}
	}
	
	synchronized void dbCheck() throws SQLException {
		if(this.db == null || this.db.isClosed()) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", this.username);
			connectionProps.put("password", this.password);
			
			this.db = DriverManager.getConnection(this.dbUrl, connectionProps); 
		}
	}
	
	synchronized void dbClose()  {
		try {
			if (!this.db.isClosed()) {
				this.db.close();
				this.db=null;
			}
		} catch (SQLException e) {
			
		}
	}
}
