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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

public class MCX700 extends BaseIC {
	
	private final String TITLE = "MELODY";
	
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
        if (sign.getLine3().length() == 0)
        {
            return "Specify song file name on line 3.";
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
    	else
    	{
    		chip.getOut(1).set(false);
    		
    		long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
    		if(!canPlay(chip, ""+curtime, chip.getText().getLine4()))
    			return;
    		
    		RedstoneListener listener = (RedstoneListener) chip.getExtra();
    		
    		chip.getText().setLine1("%"+TITLE);
    		chip.getText().setLine4(""+curtime);
    		
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
												chip.getMode() == 'd' || chip.getMode() == 'R'
												);
    		
    		music.put(""+curtime, player);
    		
    		player.loadSong();
    		
    		listener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    	}
    }
    
    protected boolean tick(Map<String,MusicPlayer> music, String id)
    {
    	if(id.isEmpty())
    		return false;
    	
    	MusicPlayer player = music.get(id);
    	if(player != null)
    	{
    		player.tick();
    		return player.isPlaying();
    	}
    	
    	return false;
    }
    
    protected boolean canPlay(ChipState chip, String curTime, String prevTime)
    {
    	if(!chip.getIn(1).is())
		{
			//turn off
			turnOff(chip);
			return false;
		}
		
		if(!chip.getIn(1).is() || !chip.getIn(1).isTriggered() || chip.getText().getLine1().charAt(0) == '%')
			return false;
		
		if(prevTime.isEmpty())
			return true;
		
		long cur = Integer.parseInt(curTime);
		long prev = Integer.parseInt(prevTime);
		
		if(cur - prev < 5)
			return false; //quick on/off protection
		
		return true;
    }
    
    protected void turnOff(ChipState chip)
    {
    	if(chip.getText().getLine4().isEmpty())
    		return;
    	
    	music.remove(chip.getText().getLine4());
    	chip.getText().setLine1("^"+TITLE);
		chip.getText().supressUpdate();
    }
    
    protected Vector findNoteBlock(ChipState chip)
    {
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	
    	Vector noteblock = Util.getWallSignBack(world, chip.getPosition(), 2);
    	if (noteblock == null) return chip.getPosition();
    	if(CraftBook.getBlockID(world, noteblock) == BlockType.NOTE_BLOCK)
    		return noteblock;
    	
    	Vector other = chip.getBlockPosition().add(0, 1, 0);
    	if(CraftBook.getBlockID(world, other) == BlockType.NOTE_BLOCK)
    		return other;
    	
    	other = Util.getWallSignSide(world, chip.getBlockPosition(), 1);
    	if(CraftBook.getBlockID(world, other) == BlockType.NOTE_BLOCK)
    		return other;
    	
    	other = Util.getWallSignSide(world, chip.getBlockPosition(), -1);
    	if(CraftBook.getBlockID(world, other) == BlockType.NOTE_BLOCK)
    		return other;
    	
    	other = chip.getBlockPosition().add(0, -1, 0);
    	if(CraftBook.getBlockID(world, other) == BlockType.NOTE_BLOCK)
    		return other;
    	
    	return noteblock;
    }
}