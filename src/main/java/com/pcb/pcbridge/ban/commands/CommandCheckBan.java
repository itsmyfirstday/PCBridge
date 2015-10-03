package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;

/**
 * Command: Retrieves data about the specified player (eg. whether currently banned, ban reason, etc)
 */

public final class CommandCheckBan implements ICommand 
{	
	public boolean Execute(CommandPacket e, Object... args) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		// retrieve ban from storage
		String username = e.Args[0];
		AbstractAdapter adapter = e.Plugin.GetAdapter();
		List<HashMap<String, Object>> results;
		try 
		{
			results = BanHelper.LookupPlayer(adapter, username, null);
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not lookup player in ban records.");
			e.Plugin.getLogger().severe("Could not lookup player in ban records: " + err.getMessage());
			return true;
		}
		
		if(results == null || results.size() == 0)
		{
			e.Sender.sendMessage(ChatColor.AQUA + username + ChatColor.WHITE + " is not currently banned.");
			return true;
		}
		
		HashMap<String, Object> ban = results.get(0);		
		
		int banDateTS 		= (int) ban.get("date_ban");
		Object banExpiryTS 	= ban.get("date_expire");
		String banStaff		= (String) ban.get("staff_name");
		String banReason 	= (String) ban.get("reason");
		
		Date banDate = new Date((long)banDateTS * 1000);
		
		String banExpiry;
		if(banExpiryTS == null)
		{
			banExpiry = "Never";
		}
		else
		{
			Date currentDate = new Date();
			long now = currentDate.getTime() / 1000;		
			long diff = now - (long)banExpiryTS;
			
			Date expiryDate = new Date(diff * 1000);
			banExpiry = expiryDate.toString();
		}
		
		String msg = ChatColor.DARK_RED + username + " is currently banned.\n\n" +
				"---\n" +
				"Reason: " + banReason + "\n" +
				"---\n" +
				"Banned by: " + banStaff + "\n" +
				"Date: " + banDate + "\n" +
				"Expires: " + banExpiry;
			
		e.Sender.sendMessage(msg);
		
		return true;
	}	
}
