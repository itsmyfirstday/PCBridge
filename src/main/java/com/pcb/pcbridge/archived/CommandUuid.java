//package com.pcb.pcbridge.archived;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.Map.Entry;
//
//import org.spigot.ChatColor;
//import org.spigot.command.CommandSender;
//import org.json.simple.parser.ParseException;
//
//import co.aikar.taskchain.TaskChain;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandUuid extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "uuid";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Looks up a player's current UUID or name change history";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.uuid";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/uuid <name>  OR  /uuid history <name>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length > 2 || args.GetArgs().length == 0)
//			return false;
//
//		if(args.GetArgs().length == 1)
//		{
//			String alias = args.GetArg(0);
//			GetCurrentUUID(args.GetSender(), alias);
//		}
//		else
//		{
//			if(!args.GetArg(0).equalsIgnoreCase("history"))
//				return false;
//
//			String alias = args.GetArg(1);
//			GetNameHistory(args.GetSender(), alias);
//		}
//
//		return true;
//	}
//
//	/**
//	 * Returns the given username's UUID
//	 *
//	 * @param sender
//	 * @param alias
//	 */
//	private void GetCurrentUUID(CommandSender sender, String alias)
//	{
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
//			.abortIfNull(AbortTask.Send(sender, ChatColor.RED + "Could not find UUID for %s. Does that player even exist?", alias))
//
//			.syncLast(uuid -> {
//				sender.sendMessage(alias + ": " + uuid);
//			})
//			.execute();
//	}
//
//	/**
//	 * Returns the given username's name change history
//	 *
//	 * @param sender
//	 * @param alias
//	 */
//	private void GetNameHistory(CommandSender sender, String alias)
//	{
//		TaskChain<?> chain = PCBridge.NewChain();
//		chain
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
//			.abortIfNull(AbortTask.Send(sender, ChatColor.RED + "Could not find UUID for %s. Does that player even exist?", alias))
//
//			.async(uuid -> {
//				chain.setTaskData("UUID", uuid);
//
//				UUIDFetcher fetcher = new UUIDFetcher();
//				try
//				{
//					return fetcher.GetNameHistory(uuid.toString());
//				}
//				catch (ParseException | IOException e)
//				{
//					sender.sendMessage(ChatColor.RED + "Failed to lookup name history");
//					e.printStackTrace();
//					return null;
//				}
//			})
//
//			.syncLast(history -> {
//				MessageBuilder builder = new MessageBuilder()
//					.Colour(ChatColor.BOLD)
//					.String("Name history of %s ", alias)
//					.Reset()
//					.String("(%s)", chain.getTaskData("UUID"))
//					.Linebreak()
//					.Stringln("---");
//
//				Iterator<Entry<String, Long>> i = history.entrySet().iterator();
//				while(i.hasNext())
//				{
//					Entry<String, Long> pair = i.next();
//
//					builder.Stringln("Name: %s", pair.getKey());
//
//					// only show the 'name change date' if it exists
//					if(pair.getValue() != null)
//					{
//						Date date = new Date();
//						date.setTime((long)pair.getValue());
//
//						builder.Stringln("Changed to on: %s", Environment.DateFormat.Long().format(date));
//					}
//
//					if(i.hasNext())
//						builder.Stringln("---");
//				}
//
//				sender.sendMessage(builder.Build());
//			})
//			.execute();
//	}
//
//}