package net.kaikk.mc.sync;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.scheduler.BukkitRunnable;

class QueryTask extends BukkitRunnable {
	@Override
	public void run() {
		if (Sync.instance==null || Sync.instance.ds==null) {
			return;
		}
		DataStore ds = Sync.instance.ds;
		
		try {
			ds.dbCheck();
			Statement statement = ds.db.createStatement();
			String sql;
			while((sql=ds.dbQueue.poll())!=null) {
				statement.executeUpdate(sql);
			}
			
			ds.dbCheck();
			ResultSet results = statement.executeQuery("SELECT * FROM sync_srv_"+Sync.instance.config.serverName);
			int count=0;
			while (results.next()) {
				Sync.instance.getLogger().info("Received command: "+results.getString(1));
				try {
					Sync.instance.getServer().dispatchCommand(Sync.instance.getServer().getConsoleSender(), results.getString(1));
				} catch (Exception e) {
					e.printStackTrace();
				}
				count++;
			}
			if (count>0) {
				statement.executeUpdate("DELETE FROM sync_srv_"+Sync.instance.config.serverName+" LIMIT "+count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		new QueryTask().runTaskLaterAsynchronously(Sync.instance, 20L);
	}
}
