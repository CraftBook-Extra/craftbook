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



import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.music.RadioObject;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX702 extends BaseIC {
    
	private final String TITLE = "MUSIC RADIO";
	
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
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a radio station name on the third line.";
        }
        
		String time = ""+System.currentTimeMillis();
		if(time.length() > 15)
			time = time.substring(time.length() - 15, time.length());
		
		sign.setLine4(time);

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
    	if(chip.inputAmount() == 0)
    	{
    		if(chip.getText().getLine1().charAt(1) == '-')
    		{
    			addRadio(chip, true);
    		}
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			chip.getText().setLine1("%-"+TITLE);
    			addRadio(chip, false);
    			chip.getText().supressUpdate();
    			
    			RedstoneListener listener = (RedstoneListener) chip.getExtra();
    			listener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else
    		{
    			chip.getText().setLine1("^"+TITLE);
    			removeRadio(chip);
    			chip.getText().supressUpdate();
    		}
    	}
    	else if(chip.getIn(2).isTriggered())
    	{
    		if(MCX701.music == null)
    			return;
    		
    		String station = chip.getText().getLine3();
        	
        	MusicPlayer player = MCX701.music.get(station);
        	
        	if(player == null)
        		return;
        	
        	String id = chip.getText().getLine4();
        	
        	RadioObject radio = player.getRadio(id);
        	if(radio != null)
        	{
        		radio.sendMessages = chip.getIn(2).is();
        	}
    	}
    }
    
    private void addRadio(ChipState chip, boolean onlyIfPlaying)
    {
    	if(MCX701.music == null)
    		return;
    	
    	String station = chip.getText().getLine3();
    	
    	MusicPlayer player = MCX701.music.get(station);
    	
    	if(player == null || (onlyIfPlaying && !player.isPlaying()))
    	{
    		chip.getOut(1).set(false);
    		return;
    	}
    	
    	String id = chip.getText().getLine4();
    	
    	Vector noteblock = findNoteBlock(chip);
    	Vector sign = chip.getPosition();
    	
    	boolean getMessage;
    	if(chip.inputAmount() == 0)
    	{
    		//can add input swap options, but I don't think it's worth it at this point
    		getMessage = Redstone.isHighBinary(chip.getCBWorld(), Util.getWallSignSide(chip.getCBWorld(), chip.getPosition(), 1), true);
    	}
    	else
    	{
    		getMessage = chip.getIn(2).is();
    	}
    	
    	RadioObject radio = new RadioObject(noteblock.getBlockX(), noteblock.getBlockY(), noteblock.getBlockZ(),
    										sign.getBlockX(), sign.getBlockY(), sign.getBlockZ(),
    										getMessage,
    										chip.getMode() == 'd'
    										);
    	
    	player.addRadio(id, radio);
    	
    	char state = chip.getText().getLine1().charAt(0);
    	chip.getText().setLine1(state+"+"+TITLE);
    	chip.getText().supressUpdate();
    	
    	chip.getOut(1).set(true);
    }
    
    private void removeRadio(ChipState chip)
    {
    	chip.getOut(1).set(false);
    	
    	if(MCX701.music == null)
    		return;
    	
    	String station = chip.getText().getLine3();
    	
    	MusicPlayer player = MCX701.music.get(station);
    	
    	if(player == null)
    		return;
    	
    	String id = chip.getText().getLine4();
    	
    	player.removeRadio(id);
    	
    	char state = chip.getText().getLine1().charAt(0);
    	chip.getText().setLine1(state+"-"+TITLE);
    	chip.getText().supressUpdate();
    }
    
    public static String getOffState(String title)
    {
    	char state = title.charAt(0);
    	return state+"-MUSIC RADIO";
    }
    
    private Vector findNoteBlock(ChipState chip)
    {
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	
    	Vector noteblock = Util.getWallSignBack(world, chip.getPosition(), 2);
    	if(noteblock == null) return chip.getPosition();
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
