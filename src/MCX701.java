import java.util.Map;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;



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





public class MCX701 extends MCX700 {
	
	private final String TITLE = "RS";
	
	/**
     * Data store.
     */
    public static Map<String,MusicPlayer> music =
            new HistoryHashMap<String,MusicPlayer>(50);
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    @Override
    public String getTitle() {
        return "^RADIO STATION";
    }
    
    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    @Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
        if (sign.getLine3().length() == 0)
        {
            return "Specify song file name on line 3.";
        }
        
        if(sign.getLine4().isEmpty())
        {
        	return "Station name is needed on line 4.";
        }
        
        String[] params = sign.getLine3().split(":", 3);
        
        if(params[0].length() == 0)
        {
        	return "Specify song file name on line 3.";
        }
        
        String[] file = params[0].split("\\.", 2);
        if(file[0].length() == 0)
        {
        	return "Specify song file name on line 3.";
        }
        
        if(!CopyManager.isValidName(file[0])
        	|| (file.length > 1
        		&& !file[1].equalsIgnoreCase("m")
        		&& !file[1].equalsIgnoreCase("p")
        		) )
        {
        	return "Not a valid song file name.";
        }
        
        int tickRate;
        if(params.length < 2 || params[1].length() == 0)
        {
        	tickRate = 4;
        }
        else
        {
        	try
        	{
        		tickRate = Integer.parseInt(params[1]);
        	}
        	catch(NumberFormatException e)
        	{
        		return "Tick rate is not a number.";
        	}
        }
        
        if(tickRate < 1)
        {
        	return "Tick rate must be 1 or up.";
        }

        return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
    	if(music == null)
    		return;
    	
    	if(chip.inputAmount() == 0)
    	{
    		chip.getOut(1).set(tick(music, chip.getText().getLine4()));
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		chip.getOut(1).set(false);
    		
    		long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
    		String time = ""+curtime;
    		if(time.length() > 12)
    			time = time.substring(time.length() - 12, time.length());
    		
    		String prevTime;
    		
    		if(chip.getText().getLine1().length() < 4 || chip.getText().getLine1().charAt(3) == 'D')
    			prevTime = "";
    		else
    			prevTime = chip.getText().getLine1().substring(3, chip.getText().getLine1().length());
    		
    		if(!canPlay(chip, time, prevTime))
    			return;
    		
    		RedstoneListener listener = (RedstoneListener) chip.getExtra();
    		
    		chip.getText().setLine1("%"+TITLE+time);
    		chip.getText().supressUpdate();
    		
    		Vector noteblockPos = findNoteBlock(chip);
    		
    		MusicPlayer player = new MusicPlayer(chip.getText().getLine3(),
    											chip.getCBWorld(),
												noteblockPos.getBlockX(),
												noteblockPos.getBlockY(),
												noteblockPos.getBlockZ(),
												listener.properties,
												(byte) 0,
												chip.getMode() == 'r' || chip.getMode() == 'R',
												chip.getMode() == 'd' || chip.getMode() == 'R',
												true);
    		
    		music.put(chip.getText().getLine4(), player);
    		
    		player.loadSong();
    		
    		listener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    	}
    	else if(chip.getIn(2).isTriggered() && chip.getIn(2).is())
    	{
    		MusicPlayer player = music.get(chip.getText().getLine4());
    		if(player != null)
    			player.playPrevious();
    	}
    	else if(chip.getIn(3).isTriggered() && chip.getIn(3).is())
    	{
    		MusicPlayer player = music.get(chip.getText().getLine4());
    		if(player != null)
    			player.playNext();
    	}
    }
    
    protected void turnOff(ChipState chip)
    {
    	String id = chip.getText().getLine1().substring(3, chip.getText().getLine1().length());
    	if(id.isEmpty())
    		return;
    	
    	MusicPlayer player = music.remove(chip.getText().getLine4());
    	if(player != null)
    	{
    		player.turnOff();
    	}
    	
    	chip.getText().setLine1("^"+TITLE+id);
		chip.getText().supressUpdate();
    }
}