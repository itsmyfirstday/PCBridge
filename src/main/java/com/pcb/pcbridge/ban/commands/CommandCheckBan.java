package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.library.TimestampHelper;
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
		
		// player is banned; compile their ban record into a nice message
		HashMap<String, Object> ban = results.get(0);		
		
		int banDateTS 		= (int) ban.get("date_ban");
		Object banExpiryTS 	= ban.get("date_expire");
		String banStaff		= (String) ban.get("staff_name");
		String banReason 	= (String) ban.get("reason");
		String banUUID		= (String) ban.get("banned_uuid");
		Date banDate 		= TimestampHelper.GetDateFromTimestamp((long)banDateTS);
		
		String banExpiry;
		String banExpiresIn = null;
		if(banExpiryTS == null)
		{
			// perma banned
			banExpiry = "Never";
		}
		else
		{
			// temp banned
			long now = TimestampHelper.GetNowTimestamp();
			
			Date expiryDate = TimestampHelper.GetDateFromTimestamp((int)banExpiryTS);
			banExpiry = expiryDate.toString();
			banExpiresIn = TimestampHelper.GetTimeDifference(now, (int)banExpiryTS);
			
			if(now >= (int)banExpiryTS)
			{
				// ban has expired
				try
				{
					if(banUUID == null)
					{
						adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE banned_name=?",
							username
						);
					}
					else
					{
						adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE banned_name=? or banned_uuid=?",
							username,
							banUUID
						);
					}
				}
				catch(SQLException err)
				{
					e.Sender.sendMessage(ChatColor.RED + "ERROR: Ban has expired but an error prevented its removal.");
					e.Plugin.getLogger().severe("Could not remove expired ban on lookup: " + err.getMessage());
					return true;
				}
				
				e.Sender.sendMessage(ChatColor.AQUA + username + ChatColor.WHITE + " is not currently banned.");
				return true;
			}
		}
		
		String msg = ChatColor.DARK_RED + username + " is currently banned.\n\n" +
				"---\n" +
				"Reason: " + banReason + "\n" +
				"---\n" +
				"Banned by: " + banStaff + "\n" +
				"Date: " + banDate + "\n" +
				"Expiry Date: " + banExpiry;
		
		if(banExpiry != "Never")
		{
			msg += "\nExpires in: " + banExpiresIn;
		}
			
		e.Sender.sendMessage(msg);
		
		return true;
	}	
}
