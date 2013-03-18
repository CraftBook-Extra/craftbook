import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.sk89q.craftbook.CraftBookEnchantment;
import com.sk89q.craftbook.CraftBookItem;
import com.sk89q.craftbook.EnchantmentType;


public class EnchantCraft
{
	private static final String PATH = "cb-enchant-recipes.txt";
	
	private static CBEnchantRecipe[] recipes = null;
	
	private static OBlock origBlockWorkbench;
	
	@SuppressWarnings("unchecked")
	protected static void load()
	{
		removeRecipes();
		
		File file = new File(PATH);
		
		if(!file.exists())
		{
			CraftBookListener.logger.info("CraftBook Enchant recipes loaded: 0 [FNF]");
			return;
		}
		
		FileInputStream fs = null;
		BufferedReader br = null;
		
		ArrayList<CBEnchantRecipe> recipes = new ArrayList<CBEnchantRecipe>();
		
		try
		{
	    	fs = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fs));
	    	
	    	ArrayList<String> recipe = null;
	    	for(String line = br.readLine(); line != null; line = br.readLine())
	    	{
	    		if(!line.isEmpty() && line.charAt(0) == '#')
	    			continue;
	    		
	    		if(line.isEmpty())
	    		{
	    			if(recipe == null)
	    				continue;
	    			if(recipe.size() != 4 && recipe.size() != 5)
	    				continue;
	    		}
	    		
	    		if(line.equalsIgnoreCase("recipe:"))
	    		{
	    			if(recipe != null)
	    			{
	    				CBEnchantRecipe enchantRecipe = parseCBEnchantRecipe(recipe, recipes.size());
	    				if(enchantRecipe == null)
	    				{
	    					CraftBookListener.logger.warning("CraftBook Enchant recipes NOT LOADED. Bad file format.");
	    					return;
	    				}
	    				recipes.add(enchantRecipe);
	    			}
	    			
	    			recipe = new ArrayList<String>();
	    		}
	    		else if(recipe == null)
	    		{
	    			CraftBookListener.logger.warning("CraftBook Enchant recipes NOT LOADED. Bad file format.");
	    			return;
	    		}
	    		else
	    		{
	    			recipe.add(line);
	    		}
	    	}
	    	
	    	if(recipe != null)
	    	{
	    		CBEnchantRecipe enchantRecipe = parseCBEnchantRecipe(recipe, recipes.size());
				if(enchantRecipe == null)
				{
					CraftBookListener.logger.warning("CraftBook Enchant recipes NOT LOADED. Bad file format.");
					return;
				}
				recipes.add(enchantRecipe);
	    	}
		}
		catch(FileNotFoundException e)
		{
			
		}
		catch(IOException e)
		{
			
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
		
		EnchantCraft.recipes = new CBEnchantRecipe[recipes.size()];
    	for(int i = 0; i < EnchantCraft.recipes.length; i++)
    	{
    		EnchantCraft.recipes[i] = recipes.get(i);
    		OCraftingManager.a().b().add(EnchantCraft.recipes[i]);
    	}
		
    	if(origBlockWorkbench != null && EnchantCraft.recipes.length == 0)
    	{
    		removeCBBlock();
    	}
    	else if(origBlockWorkbench == null && EnchantCraft.recipes.length > 0)
    	{
    		//OBlock.field_71973_m[]
	    	origBlockWorkbench = OBlock.r[Block.Type.Workbench.getType()];
	        OBlock.r[Block.Type.Workbench.getType()] = null;
	        //Block.func_71864_b("workbench")
	        OBlock.r[Block.Type.Workbench.getType()] = new CBWorkbench(Block.Type.Workbench.getType()).c("workbench");
    	}
		CraftBookListener.logger.info("CraftBook Enchant recipes loaded: "+EnchantCraft.recipes.length);
	}
	
	protected static void removeFeature()
	{
		removeCBBlock();
		removeRecipes();
	}
	
	private static void removeCBBlock()
	{
		if(origBlockWorkbench != null)
		{
			OBlock.r[Block.Type.Workbench.getType()] = null;
	        OBlock.r[Block.Type.Workbench.getType()] = origBlockWorkbench;
	        origBlockWorkbench = null;
		}
	}
	
	private static void removeRecipes()
	{
		if(recipes == null)
			return;
		
		for(int i = 0; i < recipes.length; i++)
    	{
			if(!OCraftingManager.a().b().remove(recipes[i]))
			{
				CraftBookListener.logger.warning("CraftBook failed to remove an Enchant recipe from the server.");
			}
    	}
		
		CraftBookListener.logger.info("CraftBook Enchant recipes removed: "+EnchantCraft.recipes.length);
	}
	
	private static CBEnchantRecipe parseCBEnchantRecipe(ArrayList<String> lines, int index)
	{
		if(lines == null || lines.size() <= 0)
			return null;
		
		//must have at least 6 lines (not including "recipe:" line)
		if(lines.size() < 6)
		{
			return null;
		}
		
		//enchant to add
		String[] args = lines.get(0).split("#",2);
		EnchantmentType resultEnchant = EnchantmentType.getEnchantment(args[0]);
		if(resultEnchant == null)
		{
			CraftBookListener.logger.warning("Invalid Enchantment result type for recipe #"+(index+1)+" : "+args[0]);
			return null;
		}
		
		int level = 1;
		if(args.length > 1)
		{
			try
			{
				level = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				CraftBookListener.logger.warning("Invalid Enchantment result level for recipe #"+(index+1)+" : "+args[1]);
				return null;
			}
		}
		Enchantment resultEnchantment = new Enchantment(Enchantment.Type.fromId(resultEnchant.getId()), level);
		if(!resultEnchantment.isValid())
		{
			CraftBookListener.logger.warning("Not a valid Enchantment result type for recipe #"+(index+1)+" : "+args[1]);
			return null;
		}
		
		//item to enchant
		CBEnchantRecipe.BaseItemType baseType = CBEnchantRecipe.BaseItemType.getTypeFromName(lines.get(1));
		OItemStack enchantItem = null;
		if(baseType == null)
		{
			CraftBookItem cbitem = UtilItem.parseCBItem(lines.get(1));
			if(cbitem != null)
			{
				enchantItem = new OItemStack(cbitem.id(), 1, cbitem.color());
				if(cbitem.hasEnchantments())
				{
					Item tmpItem = new Item(enchantItem);
					for(CraftBookEnchantment enchant : cbitem.enchantments())
					{
						tmpItem.addEnchantment(new Enchantment(Enchantment.Type.fromId(enchant.enchantment().getId()), enchant.level()));
					}
				}
			}
		}
		else
		{
			enchantItem = new OItemStack(baseType.ID, 1, -1);
		}
		
		if(enchantItem == null)
		{
			CraftBookListener.logger.warning("Invalid item to enchant for recipe #"+(index+1)+" : "+lines.get(1));
			return null;
		}
		
		//item definitions for 3x3 workbench
		HashMap<Character, OItemStack> definitions = new HashMap<Character, OItemStack>();
		for(int i = 5; i < lines.size(); i++)
		{
			if(lines.get(i).length() < 3)
			{
				CraftBookListener.logger.warning("Invalid item definition for recipe #"+(index+1)+" : "+lines.get(i));
				return null;
			}
			
			args = lines.get(i).substring(2).split("\\*",2);
			int amount = 1;
			if(args.length > 1)
			{
				try
				{
					amount = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException e)
				{
					CraftBookListener.logger.warning("Invalid item definition for recipe #"+(index+1)+" : "+lines.get(i));
					return null;
				}
			}
			
			CraftBookItem defItem = UtilItem.parseCBItem(args[0]);
			OItemStack oitemstack = null;
			if(defItem == null)
			{
				CBEnchantRecipe.BaseItemType defBase = CBEnchantRecipe.BaseItemType.getTypeFromName(args[0]);
				if(defBase != null)
				{
					oitemstack = new OItemStack(defBase.ID, amount, -1);
				}
			}
			else
			{
				oitemstack = new OItemStack(defItem.id(), amount, defItem.color());
				if(defItem.hasEnchantments())
				{
					Item tmpItem = new Item(oitemstack);
					for(CraftBookEnchantment enchant : defItem.enchantments())
					{
						tmpItem.addEnchantment(new Enchantment(Enchantment.Type.fromId(enchant.enchantment().getId()), enchant.level()));
					}
				}
			}
			
			if(oitemstack == null)
			{
				CraftBookListener.logger.warning("Invalid item definition for recipe #"+(index+1)+" : "+lines.get(i));
				return null;
			}
			
			definitions.put(lines.get(i).charAt(0), oitemstack);
		}
		
		//3x3 workbench rows
		int width = 0;
		int height = 0;
		final int ROWS = 3;
		final int COLS = 3;
		OItemStack[] items = new OItemStack[ROWS * COLS];
		for(int row = 0; row < ROWS; row++)
		{
			if(lines.get(row + 2).isEmpty())
				continue;
			
			for(int col = 0; col < COLS && col < lines.get(row + 2).length(); col++)
			{
				OItemStack oitemstack = definitions.get(lines.get(row + 2).charAt(col));
				if(oitemstack == null)
					continue;
				//= oitemstack.func_77946_l()
				items[(row * ROWS) + col] = oitemstack.m();
				if(row + 1 > height)
					height = row + 1;
				if(col + 1 > width)
					width = col + 1;
			}
		}
		
		if(width <= 0 || height <= 0)
		{
			CraftBookListener.logger.warning("Invalid crafting shape for recipe #"+(index+1));
			return null;
		}
		
		if(width < 3 || height < 3)
		{
			OItemStack[] tmpItems = items;
			items = new OItemStack[width * height];
			for(int i = 0; i < height; i++)
			{
				for(int j = 0; j < width; j++)
				{
					items[i * width + j] = tmpItems[i * ROWS + j];
				}
			}
		}
		
		return new CBEnchantRecipe(width, height, items, enchantItem, resultEnchantment);
	}
}
