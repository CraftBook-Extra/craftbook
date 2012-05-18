import lymia.util.Tuple2;

import com.sk89q.craftbook.BlockArea;
import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.BlockVector;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.WorldBlockVector;
import com.sk89q.craftbook.ic.ChipState;


public class UtilIC
{
	public static char getMode(String line2)
	{
		if(line2 == null || line2.isEmpty() || line2.length() < 9)
		{
			return ' ';
		}
		
		final char mode;
		String options = line2.substring(8);
		switch(options.length())
		{
			case 1: //mode
			case 3: //mode+extend
			case 4: //mode+abc | mode+def
			case 7: //mode+abc+def
				mode = options.charAt(0);
				break;
			default:
				mode = ' ';
				break;
		}
		
		return mode;
	}
	
	public static byte getExtendDirection(String line2)
	{
		if(line2 == null || line2.isEmpty())
		{
			return 0;
		}
		
		String options;
		if(line2.length() > 8)
			options = line2.substring(8);
		else
			options = line2;
		
		if(options.length() != 3 && options.length() != 2)
		{
			return 0;
		}
		
		if(options.length() == 3)
		{
			options = options.substring(1);
		}
		
		byte extend;
		if(options.equals(">>"))
		{
			extend = 1;
		}
		else if(options.equals("<<"))
		{
			extend = 2;
		}
		else if(options.equals("^^"))
		{
			extend = 3;
		}
		else if(options.equals("!^"))
		{
			extend = 4;
		}
		else
		{
			extend = 0;
		}
		
		return extend;
	}
	
	public static Sign getExtension(ChipState chip)
	{
		if(chip == null)
			return null;
		
		return getExtension(chip.getCBWorld(), chip.getPosition(), getExtendDirection(chip.getText().getLine2()));
	}
	
	public static Sign getExtension(CraftBookWorld cbworld, Vector pos, byte direction)
	{
		if(direction == 0 || cbworld == null || pos == null)
			return null;
		
		Vector loc;
		switch(direction)
		{
			case 1: //right
				loc = Util.getWallSignSide(cbworld, pos, -1);
				break;
			case 2: //left
				loc = Util.getWallSignSide(cbworld, pos, 1);
				break;
			case 3: //up
				loc = pos.add(0, 1, 0);
				break;
			case 4: //down
				loc = pos.add(0, -1, 0);
				break;
			default:
				loc = null;
				break;
		}
		
		if(loc == null || CraftBook.getBlockID(cbworld, loc) != BlockType.WALL_SIGN)
			return null;
		
		return (Sign)CraftBook.getWorld(cbworld).getComplexBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public static Tuple2<WorldBlockVector, SignText> getExtensionData(ChipState chip)
	{
		if(chip == null)
			return null;
		
		return getExtensionData(chip.getCBWorld(), chip.getPosition(), getExtendDirection(chip.getText().getLine2()));
	}
	
	public static Tuple2<WorldBlockVector, SignText> getExtensionData(CraftBookWorld cbworld, Vector pos, byte direction)
	{
		Sign sign = getExtension(cbworld, pos, direction);
		
		if(sign == null)
			return null;
		
		return new Tuple2<WorldBlockVector, SignText>(new WorldBlockVector(CraftBook.getCBWorld(sign.getWorld()), sign.getX(), sign.getY(), sign.getZ()),
								new SignText(sign.getText(0), sign.getText(1), sign.getText(2), sign.getText(3)) );
	}
	
	public static SignText getSignTextWithExtension(ChipState chip)
	{
		if(chip == null)
			return null;
		
		return getSignTextWithExtension(chip.getCBWorld(), chip.getPosition(), chip.getText(), getExtendDirection(chip.getText().getLine2()));
	}
	
	public static SignText getSignTextWithExtension(CraftBookWorld cbworld, Vector pos, SignText text)
	{
		if(text == null)
			return null;
		
		return getSignTextWithExtension(cbworld, pos, text, getExtendDirection(text.getLine2()));
	}
	
	public static SignText getSignTextWithExtension(CraftBookWorld cbworld, Vector pos, SignText text, byte direction)
	{
		Sign sign = getExtension(cbworld, pos, direction);
		
		if(sign == null)
		{
			return text;
		}
		
		return new SignText(text.getLine1()+sign.getText(0),
							text.getLine2()+sign.getText(1),
							text.getLine3()+sign.getText(2),
							text.getLine4()+sign.getText(3) );
	}
	
	public static String isValidDimensions(String settings, String lineNumber,
											final int widthMin, final int widthMax, final int heightMin, final int heightMax, final int lengthMin, final int lengthMax,
											final int offxMin, final int offxMax, final int offyMin, final int offyMax, final int offzMin, final int offzMax)
    {
    	String[] args = settings.split("/", 2);
		String[] dim = args[0].split(":", 3);
		if(dim.length != 3)
			return lineNumber+" line format: width:height:length/x-offset:y-offset:z-offset";
		try
		{
			int width = Integer.parseInt(dim[0]);
			int height = Integer.parseInt(dim[1]);
			int length = Integer.parseInt(dim[2]);
			if(width < widthMin || width > widthMax)
				return "width must be a number from "+widthMin+" to "+widthMax;
			if(height < heightMin || height > heightMax)
				return "height must be a number from "+heightMin+" to "+heightMax;
			if(length < lengthMin || length > lengthMax)
				return "length must be a number from "+lengthMin+" to "+lengthMax;
			
			if(args.length > 1)
			{
				String[] offsets = args[1].split(":", 3);
				if(offsets.length != 3)
					return lineNumber+" line format: width:height:length/x-offset:y-offset:z-offset";
				
				int offx = Integer.parseInt(offsets[0]);
				int offy = Integer.parseInt(offsets[1]);
				int offz = Integer.parseInt(offsets[2]);
				
				if(offx < offxMin || offx > offxMax)
					return "offset-x must be a number from "+offxMin+" to "+offxMax;
				if(offy < offyMin || offy > offyMax)
					return "offset-y must be a number from "+offyMin+" to "+offyMax;
				if(offz < offzMin || offz > offzMax)
					return "offset-z must be a number from "+offzMin+" to "+offzMax;
			}
		}
		catch(NumberFormatException e)
		{
			return lineNumber+" line format: width:height:length/x-offset:y-offset:z-offset";
		}
		
		return null;
    }
	
	protected static BlockArea getBlockArea(ChipState chip, int data, int width, int height, int length, int offx, int offy, int offz)
	{
		return getBlockArea(chip.getCBWorld(), chip.getPosition(), chip.getBlockPosition(), data, width, height, length, offx, offy, offz);
	}
	
	protected static BlockArea getBlockArea(CraftBookWorld cbworld, Vector signPosition, BlockVector blockPosition, int data,
											int width, int height, int length,
											int offx, int offy, int offz)
    {
    	width--;
    	height--;
    	length--;
    	
    	int wStart = width / 2;
        
        int startX = 0;
        int endX = 0;
        int startZ = 0;
        int endZ = 0;
        
        if (data == 0x2) //east
        {
        	startX = (int)signPosition.getX() - wStart;
        	endX = startX + width;
        	
        	startZ = (int)blockPosition.getZ();
        	endZ = startZ + length;
        }
        else if (data == 0x3) //west
        {
        	startX = (int)signPosition.getX() - wStart;
        	endX = startX + width;
        	
        	endZ = (int)blockPosition.getZ();
        	startZ = endZ - length;
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)signPosition.getZ() - wStart;
        	endZ = startZ + width;
        	
        	startX = (int)blockPosition.getX();
        	endX = startX + length;
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)signPosition.getZ() - wStart;
        	endZ = startZ + width;
        	
        	endX = (int)blockPosition.getX();
        	startX = endX - length;
        }
        
        int y = (int)signPosition.getY() + offy;
        
        return new BlockArea(cbworld, startX + offx, y, startZ + offz, endX + offx, y + height, endZ + offz);
    }
}
