// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.StringUtil;
import com.sk89q.craftbook.Vector;


public class PageWriter
{
	public static void handleNSCommand(Player player, String[] args, int maxCharacters, int maxPages)
	{
		if(!isValidCommandLength(player, args.length, 4))
			return;
		
		String namespace;
		if(args.length > 2 && args[2].length() > 0)
		{
			if(!player.canUseCommand("/admincbpage"))
			{
				player.sendMessage(Colors.Rose + "You are unable to use other namespaces: "+args[2]);
	    		return;
			}
			namespace = args[2];
		}
		else
			namespace = StringUtil.trimLength(player.getName(), 15);
		
		namespace = getNamespace(namespace);
		
		processCommand(player, args, namespace, true, maxCharacters, maxPages);
	}
	
	public static void handleCommand(Player player, String[] args, int maxCharacters, int maxPages)
	{
		if(!isValidCommandLength(player, args.length, 3))
			return;
		
		String namespace = StringUtil.trimLength(player.getName(), 15);
		namespace = getNamespace(namespace);
		
		processCommand(player, args, namespace, false, maxCharacters, maxPages);
	}
	
	private static void processCommand(Player player, String[] args, String namespace,
			boolean offset, int maxCharacters, int maxPages)
	{
		int start = 2;
		if(offset)
			start++;
		
		String page = "";
		if(args.length > start+1)
			page = args[start+1]; //might be part of the text
		
		String txt = "";
		if(args.length > start+2)
			txt = Util.joinString(args, " ", start+2);
		
		processCommand(player, args[1], namespace, args[start], page, txt, maxCharacters, maxPages);
	}
	
	private static void processCommand(Player player, String command, String namespace, String book, String page,
			String text, int maxCharacters, int maxPages)
	{
		if(namespace.length() <= 0)
		{
			player.sendMessage(Colors.Rose + "Invalid namespace: "+namespace);
    		return;
		}
		
    	if(!CopyManager.isValidName(book))
		{
			player.sendMessage(Colors.Rose + "Invalid book name: "+book);
    		return;
		}
    	
    	if(command.equalsIgnoreCase("create") || command.equalsIgnoreCase("delete"))
    	{
    		if(command.equalsIgnoreCase("delete"))
    		{
    			if(deleteBook(namespace, book))
    				player.sendMessage(Colors.Gold + "Deleted "+book+" from "+namespace);
    			else
    				player.sendMessage(Colors.Rose + "Failed to delete "+book+" from "+namespace);
    			return;
    		}
    		else
    		{
    			if(createBook(namespace, book))
    				player.sendMessage(Colors.Gold + "Created "+book+" at "+namespace);
    			else
    				player.sendMessage(Colors.Rose + "Failed to create "+book+" at "+namespace);
    			return;
    		}
    	}
    	else if(command.equalsIgnoreCase("settitle") || command.equals("+") || command.equalsIgnoreCase("add") ||
    			command.equals("=") || command.equals("^") || command.equalsIgnoreCase("remove"))
    	{
    		int pagenum = -1;
    		if(command.equalsIgnoreCase("settitle"))
    		{
    			pagenum = 0;
    			text = page + " " + text;
    		}
    		else if(command.equalsIgnoreCase("add"))
        	{
        		text = page + " " + text;
        	}
    		else if(page.length() != 0)
    		{
    			try
    			{
    				pagenum = Integer.parseInt(page);
    			}
    			catch(NumberFormatException e)
    			{
    				player.sendMessage(Colors.Rose + "Invalid page number: "+page);
    	    		return;
    			}
    			
    			if(pagenum > maxPages || pagenum < 1)
        		{
        			player.sendMessage(Colors.Rose + "Page number must be between 1 and "+maxPages);
    	    		return;
        		}
    		}
    		else
    		{
    			player.sendMessage(Colors.Rose + "Page number required for command: "+command);
	    		return;
    		}
    		
    		int type = 0;
    		if(command.equals("+"))
    			type = 1;
    		else if(command.equalsIgnoreCase("remove"))
    			type = 2;
    		else if(command.equals("^"))
    			type = 3;
    		
    		if(writePage(namespace, book, pagenum, text, maxCharacters, maxPages, type))
    		{
    			player.sendMessage(Colors.Gold + "Changes were applied.");
	    		return;
    		}
    		else
    		{
    			player.sendMessage(Colors.Rose + "Failed to write to "+namespace+"/"+book+". May have reached page max");
	    		return;
    		}
    	}
    	else if(command.equalsIgnoreCase("page") || command.equalsIgnoreCase("title"))
    	{
    		int pagenum = -1;
    		if(command.equalsIgnoreCase("title"))
    		{
    			pagenum = 0;
    		}
    		else if(page.length() != 0)
    		{
    			try
    			{
    				pagenum = Integer.parseInt(page);
    			}
    			catch(NumberFormatException e)
    			{
    				player.sendMessage(Colors.Rose + "Invalid page number: "+page);
    	    		return;
    			}
    			
    			if(pagenum > maxPages || pagenum < 1)
        		{
        			player.sendMessage(Colors.Rose + "Page number must be between 1 and "+maxPages);
    	    		return;
        		}
    		}
    		else
    		{
    			player.sendMessage(Colors.Rose + "Page number required for command: "+command);
	    		return;
    		}
    		
    		String[] output = getPage(namespace, book, pagenum);
    		
    		if(output == null)
    		{
    			player.sendMessage(Colors.Rose + "Failed to fetch from "+namespace+"/"+book+".");
    			return;
    		}
    		
    		if(pagenum == 0)
    		{
    			if(output[0] == null)
    				player.sendMessage(Colors.Rose+"title is blank.");
    			else
    				player.sendMessage(Colors.Gold+output[0]);
    		}
    		else
    		{
    			if(output[1] == null)
    				output[1] = "";
    			player.sendMessage(output[1]);
    		}
    	}
    	else
    	{
    		player.sendMessage(Colors.Rose + "Unknown /cbpage command: "+command);
			return;
    	}
	}
	
	private static boolean isValidCommandLength(Player player, int length, int minLength)
	{
		if(length == 1)
    	{
			player.sendMessage(Colors.Rose + "/cbpage page [book] [page#]"+Colors.White+" - displays page");
    		player.sendMessage(Colors.Rose + "/cbpage create [book]"+Colors.White+" - creates book .txt file");
    		player.sendMessage(Colors.Rose + "/cbpage delete [book]"+Colors.White+" - deletes book .txt file");
    		player.sendMessage(Colors.Rose + "/cbpage title [book]"+Colors.White+" - displays current title.");
    		player.sendMessage(Colors.Rose + "/cbpage settitle [book]"+Colors.White+" - sets the book's title.");
    		player.sendMessage(Colors.Rose + "/cbpage add [book] [text]"+Colors.White+" - adds page to end of book.");
    		player.sendMessage(Colors.Rose + "/cbpage remove [book] [page#]"+Colors.White+" - removes page.");
    		player.sendMessage(Colors.Rose + "/cbpage = [book] [page#] [text]"+Colors.White+" - sets page text.");
    		player.sendMessage(Colors.Rose + "/cbpage + [book] [page#] [text]"+Colors.White+" - appends to page.");
    		player.sendMessage(Colors.Rose + "/cbpage ^ [book] [page#] [text]"+Colors.White+" - inserts page.");
    		return false;
    	}
    	
    	if(length < minLength)
    	{
    		player.sendMessage(Colors.Rose + "Unknown /cbpage command.");
    		return false;
    	}
    	
    	return true;
	}
	
	public static void resetPage(Sign sign)
	{
		String[] args = sign.getText(3).split(":", 3);
		if(args.length < 3)
			return;
		
		try
		{
			int start = Integer.parseInt(args[1]);
			
			sign.setText(3, (start-1)+":"+args[1]+":"+args[2]);
			//shouldn't need to update
		}
		catch(NumberFormatException e)
		{
			return;
		}
	}
	
	
	/////////////////////////////////////
	/////////////////////////////////////
	
	
	public static void readPage(Player player, Sign sign)
	{
		String dir = getNamespace(sign.getText(0));
		String book = sign.getText(2);
    	if(dir.length() == 0 || book.length() == 0)
    	{
    		player.sendMessage(Colors.Rose + "Failed to get proper namespace "+dir+" or book name was blank.");
    		return;
    	}
		
		String[] args = sign.getText(3).split(":", 3);
    	
    	int page;
    	
    	try
		{
        	if(args.length == 1)
        	{
        		if(args[0].length() == 0)
        			page = 1;
        		else
        			page = Integer.parseInt(args[0]);
        	}
        	else if(args.length == 2)
        	{
        		//page will become "start" integer
        		page = Integer.parseInt(args[0]);
        		sign.setText(3, args[0]+":"+args[0]+":"+args[1]);
        		sign.update();
        	}
        	else
        	{
        		page = Integer.parseInt(args[0]);
        		int start = Integer.parseInt(args[1]);
        		int end = Integer.parseInt(args[2]);
        		
        		if(page >= end || page >= 9999)
        			page = start;
        		else if(page < start)
        			page = start;
        		else
        			page++;
        		
        		sign.setText(3, page+":"+args[1]+":"+args[2]);
        		//shouldn't need to update
        	}
		}
    	catch(NumberFormatException e)
    	{
    		player.sendMessage(Colors.Rose + "Page, start, or end values on fourth line were not numbers.");
    		return;
    	}
    	
    	PageWriter.readPage(player, dir, book, page);
	}
	
	public static void readPage(Player player, String dir, String book, int page)
	{
		String[] text = getPage(dir, book, page);
		
		String pageInfo = "";
		if(page > 1)
			pageInfo = Colors.LightGray + " ["+page+"]";
		
		if(text != null && text.length > 1)
		{
			if(text[0] != null && text[0].length() > 0)
				player.sendMessage(Colors.Gold + text[0] + pageInfo);
			
			if(text[1] == null)
				text[1] = "";
			
			String[] lines = text[1].split("<br>", 20); //max 20 so that it doesn't completely flood a user's chat
			for(String line : lines)
			{
				player.sendMessage(line);
			}
		}
		else
		{
			player.sendMessage(Colors.Rose + "Failed to fetch page "+page+" from "+dir+"/"+book+".");
		}
	}
	
	private static String[] getPage(String dir, String book, int page)
    {
		File file = new File("cbbooks" + File.separator +
							dir + File.separator + 
							book + ".txt");
		
		if (!file.exists())
			return null;
		
		FileInputStream fs = null;
		BufferedReader br = null;
		
		String[] out = null;
		
		try
		{
	    	fs = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fs));
	    	
	    	out = new String[2];
	    	
	    	//book title
	    	out[0] = br.readLine();
	    	
	    	//if just want title
	    	if(page == 0)
	    		return out;
	    	
	    	//skip to "page"
	    	for(int i = 1; i < page; ++i)
	    	{
	    		if(br.readLine() == null)
	    			break; //EoF, anything after this will be null
	    	}
	    	
	    	//page
	    	out[1] =  br.readLine();
		}
		catch(FileNotFoundException e)
		{
			out = null;
		}
		catch(IOException e)
		{
			out = null;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e)
			{
				
			}
		}
    	
    	return out;
    }
	
	private static boolean writePage(String dir, String book, int page, String text,
			int maxCharacters, int maxPages, int type)
	{
		if(page < -1 || page > maxPages)
			return false;
		
		File file = new File("cbbooks" + File.separator +
							dir + File.separator + 
							book + ".txt");
		
		if (!file.exists())
			return false;
		
		
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		
		//only stores the max amount of pages allowed +1 for the title
		//other lines after this amount will be deleted
		String[] txt = new String[maxPages+1];
		
		boolean write = false;
		
		try
		{
			//read file into memory... yah...
	    	fis = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fis));
	    	
	    	for(int i = 0; i < txt.length; i++)
	    	{
	    		if(i == page)
	    		{
	    			switch(type)
	    			{
	    				case 1: //append to existing line
	    					String line = br.readLine();
	    	    			if(line == null)
	    	    				line = "";
	    	    			
	    					txt[i] = StringUtil.trimLength(line+text, maxCharacters);
	    					break;
	    				case 2: //remove
	    					br.readLine();
	    					txt[i] = "";
	    					break;
	    				case 3: //insert line
	    					txt[i] = StringUtil.trimLength(text, maxCharacters);
	    					break;
	    				default: //set line
	    					br.readLine();
	    					txt[i] = StringUtil.trimLength(text, maxCharacters);
	    					break;
	    			}
	    			
	    			write = true;
	    		}
	    		else
	    		{
	    			String line = br.readLine();
	    			if(line == null)
	    			{
	    				if(page == -1)
	    				{
	    					//append to end (and within max pages amount)
	    					txt[i] = StringUtil.trimLength(text, maxCharacters);
	    					write = true;
	    					break;
	    				}
	    				if(i < page)
	    					line = "";
	    				else
	    					break; //reached end of pages
	    			}
	    			
	    			txt[i] = StringUtil.trimLength(line, maxCharacters);
	    		}
	    	}
	    	
	    	if(!write)
	    		return false;
	    	
	    	//write file
	    	fos = new FileOutputStream(file, false);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			
			for(String line : txt)
			{
				if(line == null)
					break;
				
				writer.write(line);
				writer.newLine();
			}
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
		catch(IOException e)
		{
			return false;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e) {}
			finally
			{
				try
				{
					if(writer != null)
						writer.close();
				}
				catch(IOException e) {}
			}
		}
		
		return true;
	}
	
	
	private static boolean createBook(String dir, String book)
	{
		File file = new File("cbbooks" + File.separator +
				dir + File.separator + 
				book + ".txt");
		
		if (file.exists())
			return true;
		
		File folder = file.getParentFile();
		if(!folder.exists()) 
		{
			if(!folder.mkdirs())
				return false;
		}
		
		
		try
		{
			file.createNewFile();
		}
		catch(IOException e)
		{
			return false;
		}
		
		return true;
	}
	
	
	private static boolean deleteBook(String dir, String book)
	{
		File file = new File("cbbooks" + File.separator +
				dir + File.separator + 
				book + ".txt");
		
		if (!file.exists())
			return true;
		
		return file.delete();
	}
	
	/////////////////////////////////////
	/////////////////////////////////////
	
	/**
	 * Validates the sign's environment.
	 * 
	 * @param signText
	 * @return false to deny
	 */
	
	public static boolean validateEnvironment(CraftBookPlayer player, Vector pt, SignText signText)
	{
		World world = CraftBook.getWorld(player.getCBWorld());
		Vector bPt = Util.getWallSignBack(world, pt, 1);
		if (bPt == null) return false;
		if(CraftBook.getBlockID(world, pt) != BlockType.WALL_SIGN || CraftBook.getBlockID(world, bPt) != BlockType.BOOKCASE)
		{
			player.printError("[Book] sign must be on Bookshelf.");
			return false;
		}
		
		String playerNS = StringUtil.trimLength(player.getName(), 15);
		String wantedNS = signText.getLine1();
		String bookid = signText.getLine3();
		String pageOptions = signText.getLine4();
		
		if (!CopyManager.isValidName(bookid))
		{
			player.printError("An invalid book id name was indicated.");
			return false;
		}
		
		if (wantedNS.length() == 0 || wantedNS.equalsIgnoreCase(playerNS))
		{
			if(player.hasPermission("denypagereaders") && !player.hasPermission("makeallpagereaders"))
			{
				player.printError("You are unable to make page readers in your namespace.");
				return false;
			}
			
			signText.setLine1(playerNS);
		}
		else if(wantedNS.equals("@"))
		{
			if(player.hasPermission("denyglobalpagereaders") && !player.hasPermission("makeallpagereaders"))
			{
				player.printError("You are unable to make global namespace page readers.");
				return false;
			}
		}
		else if(!player.hasPermission("makeallpagereaders"))
		{
			player.printError("You are unable to use other namespaces for page readers.");
			return false;
		}
		
		if(pageOptions.length() > 0)
		{
			String[] options = pageOptions.split(":");
			
			if(options.length > 3)
			{
				player.printError("Incorrect page options used on fourth line.");
				return false;
			}
			
			try
			{
				for(String val : options)
				{
					int num = Integer.parseInt(val);
					if(num > 9999)
					{
						player.printError("Page values can only be up to 9999.");
						return false;
					}
				}
			}
			catch(NumberFormatException e)
			{
				player.printError("Page values must be a number.");
				return false;
			}
		}
		
		player.print("Page reader created!");
		
		return true;
	}
	
	public static String getNamespace(String namespace)
	{
		if (namespace.equals("@"))
			return "global";
		
		if (CopyManager.isValidNamespace(namespace))
			return "~" + namespace;
		
		return "";
	}
}
