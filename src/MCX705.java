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




import java.util.Map;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

public class MCX705 extends MCX700 {
	
	private final String TITLE = "TUNE";
	
	/**
     * Data store.
     */
    public static Map<String,MusicPlayer> music =
            new HistoryHashMap<String,MusicPlayer>(100);
    
    /**
     * Get the title of the IC.
     *
     * @return
     */
    @Override
    public String getTitle() {
        return "^"+TITLE;
    }
    
    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    @Override
    public boolean requiresPermission() {
        return true;
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
    	String tune = sign.getLine3() + sign.getLine4();
        if (tune.length() == 0)
        {
            return "Specify tune on line 3.";
        }
        
        String[] params = tune.split(":", 2);
        
        if(params[0].length() == 0)
        {
        	return "Specify tune on line 3.";
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
        		tickRate = Integer.parseInt(params[0]);
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
    		String id = chip.getText().getLine1().substring(5, chip.getText().getLine1().length());
    		chip.getOut(1).set(tick(music, id));
    	}
    	else
    	{
    		chip.getOut(1).set(false);
    		
    		long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
    		String time = ""+curtime;
    		if(time.length() > 10)
    			time = time.substring(time.length() - 10, time.length());
    		
    		String prevTime = chip.getText().getLine1().substring(5, chip.getText().getLine1().length());
    		
    		if(!canPlay(chip, time, prevTime))
    			return;
    		
    		RedstoneListener listener = (RedstoneListener) chip.getExtra();
    		
    		chip.getText().setLine1("%"+TITLE+time);
    		chip.getText().supressUpdate();
    		
    		Vector noteblockPos = findNoteBlock(chip);
    		
    		MusicPlayer player = new MusicPlayer(chip.getText().getLine3() + chip.getText().getLine4(),
												chip.getCBWorld(),
												noteblockPos.getBlockX(),
												noteblockPos.getBlockY(),
												noteblockPos.getBlockZ(),
												null,
												(byte) 1,
												false,
												chip.getMode() == 'd'
												);
    		
    		music.put(time, player);
    		
    		player.loadSong();
    		
    		listener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    	}
    }
    
    protected void turnOff(ChipState chip)
    {
    	String id = chip.getText().getLine1().substring(5, chip.getText().getLine1().length());
    	if(id.isEmpty())
    		return;
    	
    	music.remove(id);
    	chip.getText().setLine1("^"+TITLE+id);
		chip.getText().supressUpdate();
    }
}