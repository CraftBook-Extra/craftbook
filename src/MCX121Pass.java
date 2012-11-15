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

public class MCX121Pass
{
	/**Checks if the passwords match
	 * 
	 * @param name
	 * @param password
	 * @return Returns true if passwords matched, false otherwise. Will return null if
	 * password file does not exist.
	 */
	public static Boolean isPassword(String name, String password)
	{
		String pass = getPass(name);
		
		if(pass == null)
			return null;
		if(pass.length() == 0)
			return false;
		
		return pass.equals(encrypt(password));
	}
	
	public static Boolean hasPassword(String name)
	{
		String pass = getPass(name);
		if(pass == null)
			return null;
		
		return pass.length() > 0;
	}
	
	/** Gets password from file. Using file instead of storing in memory because I
	 * do not believe this feature will be used too often.
	 * 
	 * @param name
	 * @return Returns password, empty string if no password was found, or null if
	 * file was not found.
	 */
	private static String getPass(String name)
    {
		if(name.length() == 0)
			return "";
		
		File file = new File("world" + File.separator +
							"craftbook" + File.separator +
							"mcx121.txt");
		
		if (!file.exists())
			return null;
		
		FileInputStream fs = null;
		BufferedReader br = null;
		
		String pass = "";
		
		try
		{
	    	fs = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fs));
	    	
	    	String line = null;
	    	while((line = br.readLine()) != null)
	    	{
	    		String[] args = line.split(":", 2);
	    		if(args[0].equals(name))
	    		{
	    			if(args.length > 1)
	    				pass = args[1];
	    			break;
	    		}
	    	}
		}
		catch(FileNotFoundException e)
		{
			pass = "";
		}
		catch(IOException e)
		{
			pass = "";
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
    	
    	return pass;
    }
	
	/** Currently NOT a real encryption.
	 * 
	 * @param pass
	 * @return Returns the "encrypted" string, which is currently not a real encryption.
	 */
	private static String encrypt(String pass)
	{
		//obviously this is NOT a real encryption, but
		//leaving this here in case someone wants to
		//create a real encryption.
		//I feel a high/medium security isn't needed for
		//something like this.
		return ""+pass.hashCode();
	}
	
	public static Boolean setPassword(String name, String password)
	{
		return setPassword(name, password, "", false);
	}
	
	/**
	 * 
	 * @param name
	 * @param password
	 * @param newPassword
	 * @return Returns true if password was set. Returns false if was not set from either a bad
	 * original password or an invalid name. Returns null if there was a file error.
	 */
	public static Boolean setPassword(String name, String password, String newPassword)
	{
		return setPassword(name, password, newPassword, false);
	}
	
	
	public static Boolean setPassword(String name, String password, String newPassword, boolean remove)
	{
		if(name.length() == 0 || password.length() == 0 || !CopyManager.isValidName(name))
			return false;
		
		File file = new File("world" + File.separator +
							"craftbook" + File.separator +
							"mcx121.txt");

		if (!file.exists())
		{
			File folder = file.getParentFile();
			if(!folder.exists()) 
			{
				if(!folder.mkdirs())
					return null;
			}
			
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				return null;
			}
		}
		
		//create a temp file save system?
		//maybe not for something as small as this...
		
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		
		//only allows a storing of 100 passwords - why? this is totally arbitrary. - nosefish
		//increasing the limit to 1024 (arbitrary? sure!), but this will have to be recoded eventually
		String[] txt = new String[1024];
		
		boolean write = false;
		
		password = encrypt(password);
		
		try
		{
			//read file into memory... yah...
	    	fis = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fis));
	    	
	    	for(int i = 0; i < txt.length; i++)
	    	{
	    		String line = br.readLine();
	    		
	    		if(line == null)
	    		{
	    			if(!write)
	    			{
	    				if(remove)
	    				{
	    					return true;
	    				}
	    				
		    			if(newPassword.length() > 0)
		    			{
		    				//even though password doesn't exist, so the current set password
		    				// IS WRONG, we'll let the player have the new pass anyways...
		    				txt[i] = name+":"+encrypt(newPassword);
		    			}
		    			else
		    			{
		    				txt[i] = name+":"+password;
		    			}
		    			write = true;
	    			}
	    			
	    			//EoF
	    			break;
	    		}
	    		else
	    		{
	    			if(!write)
	    			{
		    			String[] args = line.split(":", 2);
		    			if(args[0].equalsIgnoreCase(name))
		    			{
		    				if(remove)
		    				{
		    					write = true;
		    					i--;
		    					continue;
		    				}
		    				
		    				if(newPassword.length() == 0)
		    					return false; //can't change existing password
		    				
		    				if(args.length > 1)
		    				{
		    					if(args[1].equals(password))
		    					{
		    						txt[i] = name+":"+encrypt(newPassword);
		    						write = true;
		    					}
		    					else
		    					{
		    						return false; //bad original password
		    					}
		    				}
		    				else
		    				{
		    					//password is blank? must be a direct file edit.
		    					//return false since the passwords obviously don't match
		    					return false;
		    				}
		    			}
	    			}
	    			
	    			if(txt[i] == null)
	    				txt[i] = line;
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
			return null;
		}
		catch(IOException e)
		{
			return null;
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
}
