// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX146 extends BaseIC {
	
	protected static final Map<Player, ArrayList<PotionEffectObject>> playerList = new WeakHashMap<Player, ArrayList<PotionEffectObject>>();
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	protected String settings = "";
	private final String TITLE = "POTION";
	
	@Override
    public String getTitle() {
    	return "^"+TITLE+settings;
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
    	
		sign = UtilIC.getSignTextWithExtension(cbworld, pos, sign);
		
    	if(PotionType.getEffect(sign.getLine3()) == null)
    	{
    		return "3rd line must contain a valid Potion name or id #";
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "4th", 1,16, 1,16, 1,16,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
    	if(!sign.getLine1().isEmpty())
    	{
    		try
    		{
    			String[] args = sign.getLine1().split(":", 2);
    			int amplifier = Integer.parseInt(args[0]);
    			if(amplifier < 0)
    				amplifier = 0;
    			else if(amplifier > 15)
    				amplifier = 15;
    			
    			settings = " +"+amplifier;
    			
    			if(args.length > 1)
    			{
    				int duration = Integer.parseInt(args[1]);
    				if(duration < 5)
    					duration = 5;
    				else if(duration > 300)
    					duration = 300;
    				
    				settings += ":"+duration;
    			}
    			
    		}
    		catch(NumberFormatException e)
    		{
    			return "1st line must be a number from 0 to 15 or blank";
    		}
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
    	if(chip.inputAmount() == 0
    		|| (chip.getText().getLine2().charAt(3) == 'X' && chip.getIn(1).isTriggered() && chip.getIn(1).is())
    		)
    	{
    		PotionType type = PotionType.getEffect(chip.getText().getLine3());
        	if(!type.allowed)
        		return;
    		
	    	int width = 3;
	    	int height = 1;
	    	int length = 3;
	    	int offx = 0;
	    	int offy = 1;
	    	int offz = 0;
	    	
	    	if(!chip.getText().getLine4().isEmpty())
	    	{
	    		String[] args = chip.getText().getLine4().split("/", 2);
	    		String[] dim = args[0].split(":", 3);
	    		
	    		if(dim.length != 3)
	    			return;
	    		
	    		width = Integer.parseInt(dim[0]);
	    		height = Integer.parseInt(dim[1]);
	    		length = Integer.parseInt(dim[2]);
	    		
	    		if(args.length > 1)
	    		{
	    			String[] offsets = args[1].split(":", 3);
	    			offx = Integer.parseInt(offsets[0]);
	    			offy = Integer.parseInt(offsets[1]);
	    			offz = Integer.parseInt(offsets[2]);
	    		}
	    	}
	    	
	    	if(width > RedstoneListener.icInAreaMaxLength)
	    		width = RedstoneListener.icInAreaMaxLength;
	    	if(length > RedstoneListener.icInAreaMaxLength)
	    		length = RedstoneListener.icInAreaMaxLength;
	    	
	    	World world = CraftBook.getWorld(chip.getCBWorld());
	    	Vector lever = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
	        int data = CraftBook.getBlockData(world, chip.getPosition());
	        BlockArea area = UtilIC.getBlockArea(chip, data, width, height, length, offx, offy, offz);
	        
	        detectPlayers(world, lever, area, chip);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			chip.getText().setLine1("%"+chip.getText().getLine1().substring(1));
    			chip.getText().supressUpdate();
    			
    			RedstoneListener redListener = (RedstoneListener) chip.getExtra();
    			redListener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			chip.getText().setLine1("^"+chip.getText().getLine1().substring(1));
    			chip.getText().supressUpdate();
    		}
    	}
    }
    
    protected void detectPlayers(World world, Vector lever, BlockArea area, ChipState chip)
    {
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	PotionType type = PotionType.getEffect(text.getLine3());
    	if(!type.allowed)
    		return;
    	
    	String[] args = text.getLine1().split("\\+", 2);
    	int amplifier = 0;
    	int duration = 180 * 20;
    	if(args.length > 1)
    	{
    		args = args[1].split(":", 2);
    		amplifier = Integer.parseInt(args[0]);
    		
    		if(args.length > 1 && chip.inputAmount() > 0)
    		{
    			duration = Integer.parseInt(args[1]) * 20;
    		}
    	}
        
    	AreaPotionEffect areaPotion = new AreaPotionEffect(area, lever, chip.inputAmount() == 0, type, duration, amplifier);
        etc.getServer().addToServerQueue(areaPotion);
    }
    
    public class AreaPotionEffect implements Runnable
    {
    	private final BlockArea AREA;
    	private final Vector LEVER;
    	private final boolean AUTO;
    	private final PotionType POTION;
    	private final int DURATION;
    	private final int AMPLIFIER;
    	
    	public AreaPotionEffect(BlockArea area, Vector lever, boolean auto, PotionType potionType, int duration, int amplifier)
    	{
    		AREA = area;
    		LEVER = lever;
    		AUTO = auto;
    		POTION = potionType;
    		DURATION = duration;
    		AMPLIFIER = amplifier;
    	}
    	
		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			boolean output = false;
			
			List<Player> players = etc.getServer().getPlayerList();
			
			for(Player player : players)
			{
				if(AREA.containsPoint(AREA.getCBWorld(),
									OMathHelper.c(player.getX()),
									OMathHelper.c(player.getY()),
									OMathHelper.c(player.getZ()) )
					)
				{
					output = true;
					
					PotionEffect potion = PotionEffect.getNewPotionEffect(PotionEffect.Type.fromId(POTION.getId()), AMPLIFIER, DURATION);
					
					if(AUTO)
					{
						if(!playerList.containsKey(player))
						{
							playerList.put(player, new ArrayList<PotionEffectObject>());
						}
						
						ArrayList<PotionEffectObject> potionList = playerList.get(player);
						for(int i = 0; i < potionList.size(); i++)
						{
							PotionEffect potionEffect = potionList.get(i).POTION;
							if(potionEffect.getType().getId() == POTION.getId())
							{
								OPotionEffect opeffect = player.getEntity().b(OPotion.a[POTION.getId()]);
								if(opeffect != null)
								{
									PotionEffect potion2;
									if(opeffect.potionEffect == null)
										potion2 = new PotionEffect(opeffect);
									else
										potion2 = opeffect.potionEffect;
									
									if(potion2.getDuration() < 2 * 20)
									{
										potion2.setDuration(DURATION);
										UtilEntity.sendPacket(player.getEntity(), new OPacket41EntityEffect(player.getEntity().hashCode(), opeffect));
									}
								}
								else
								{
									PlayerSettings settings = CraftBookListener.getPlayerSettings(player);
									if(settings == null || settings.activePotionsMap == null)
									{
										return;
									}
									settings.activePotionsMap.put(Integer.valueOf(POTION.getId()), potion.potionEffect);
									UtilEntity.sendPacket(player.getEntity(), new OPacket41EntityEffect(player.getEntity().hashCode(), potion.potionEffect));
								}
								return;
							}
						}
						
						OPotionEffect opeffect = player.getEntity().b(OPotion.a[POTION.getId()]);
						if(opeffect != null)
						{
							PotionEffect oldPotion;
							if(opeffect.potionEffect == null)
								oldPotion = new PotionEffect(opeffect);
							else
								oldPotion = opeffect.potionEffect;
							
							potionList.add(new PotionEffectObject(potion, oldPotion.getDuration(), oldPotion.getAmplifier()));
						}
						else
						{
							potionList.add(new PotionEffectObject(potion));
						}
					}
					
					
					OEntityPlayerMP oplayer = player.getEntity();
					OPotionEffect opotion = potion.potionEffect;
					
					if(oplayer.a(OPotion.a[POTION.getId()]))
					{
						OPotionEffect opeffect = oplayer.b(OPotion.a[POTION.getId()]);
						opeffect.a(opotion);
						UtilEntity.sendPacket(oplayer, new OPacket41EntityEffect(oplayer.hashCode(), oplayer.b(OPotion.a[POTION.getId()])));
					}
					else
					{
						PlayerSettings settings = CraftBookListener.getPlayerSettings(player);
						if(settings == null || settings.activePotionsMap == null)
						{
							return;
						}
						
						settings.activePotionsMap.put(Integer.valueOf(POTION.getId()), opotion);
						UtilEntity.sendPacket(oplayer, new OPacket41EntityEffect(oplayer.hashCode(), opotion));
					}
				}
				else if(AUTO && playerList.containsKey(player))
				{
					ArrayList<PotionEffectObject> potionList = playerList.get(player);
					for(int i = 0; i < potionList.size(); i++)
					{
						if(potionList.get(i).POTION.getType().getId() == POTION.getId())
						{
							PotionEffectObject potionObject = potionList.remove(i);
							
							OPotionEffect opeffect = player.getEntity().b(OPotion.a[POTION.getId()]);
							PotionEffect potion;
							
							if(opeffect != null)
							{
								if(opeffect.potionEffect == null)
									potion = new PotionEffect(opeffect);
								else
									potion = opeffect.potionEffect;
								
								if(potionObject.timeRemaining() <= 1)
									potion.setDuration(1 * 20);
								else
									potion.setDuration(potionObject.timeRemaining());
								
								if(potionObject.amplifier() >= 0 && potion.getAmplifier() != potionObject.amplifier())
								{
									potion.setAmplifier(potionObject.amplifier());
									UtilEntity.sendPacket(player.getEntity(), new OPacket42RemoveEntityEffect(player.getEntity().hashCode(), potion.potionEffect));
								}
								UtilEntity.sendPacket(player.getEntity(), new OPacket41EntityEffect(player.getEntity().hashCode(), potion.potionEffect));
							}
							
							if(potionList.size() <= 0)
							{
								playerList.remove(player);
							}
							
							return;
						}
					}
				}
			}
			if (LEVER != null)
				Redstone.setOutput(AREA.getCBWorld(), LEVER, output);
		}
    }
    
    public class PotionEffectObject
    {
    	public final PotionEffect POTION;
    	private int timeRemaining = 0;
    	private int amplifier = -1;
    	
    	
    	public PotionEffectObject(PotionEffect potion)
    	{
    		this(potion, 0, -1);
    	}
    	
    	public PotionEffectObject(PotionEffect potion, int timeRemaining, int amplifier)
    	{
    		POTION = potion;
    		this.timeRemaining = timeRemaining;
    		this.amplifier = amplifier;
    	}
    	
    	public int timeRemaining()
    	{
    		return timeRemaining;
    	}
    	
    	public int amplifier()
    	{
    		return amplifier;
    	}
    }
}
