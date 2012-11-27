

public class CBEnchantRecipe extends OShapedRecipes
{
	protected enum BaseItemType
	{
		HELMET(-20, new int[]{298, 302, 306, 310, 314}),
		CHESTPLATE(-21, new int[]{299, 303, 307, 311, 315}),
		LEGGINGS(-22, new int[]{300, 304, 308, 312, 316}),
		BOOTS(-23, new int[]{301, 305, 309, 313, 317}),
		SWORD(-24, new int[]{268, 272, 267, 276, 283}),
		SHOVEL(-25, new int[]{269, 273, 256, 277, 284}),
		PICKAXE(-26, new int[]{270, 274, 257, 278, 285}),
		AXE(-27, new int[]{271, 275, 258, 279, 286}),
		BOW(-28, new int[]{261}),
		HOE(-29, new int[]{290, 291, 292, 293, 294}),
		;
		
		public final int ID;
		private final int[] SUBTYPES;
		
		private BaseItemType(int id, int[] subtypes)
		{
			ID = id;
			SUBTYPES = subtypes;
		}
		
		public boolean isSameType(int id)
		{
			if(SUBTYPES == null)
				return false;
			
			for(int i = 0; i < SUBTYPES.length; i++)
			{
				if(SUBTYPES[i] == id)
					return true;
			}
			
			return false;
		}
		
		public static BaseItemType fromId(int id)
		{
			BaseItemType[] types = BaseItemType.values();
			
			for(BaseItemType type : types)
			{
				if(type.ID == id)
				{
					return type;
				}
			}
			
			return null;
		}
		
		public static BaseItemType getTypeFromName(String name)
		{
			if(name == null || name.isEmpty())
				return null;
			
			name = name.toUpperCase();
			name = name.replace(' ', '_');
			try
			{
				return BaseItemType.valueOf(name);
			}
			catch(IllegalArgumentException e)
			{
				
			}
			return null;
		}
	}
	
	
    private int recipeWidth;
    private int recipeHeight;
    private OItemStack recipeItems[];
	private final Enchantment RESULT;
	
	public CBEnchantRecipe(int recipeWidth, int recipeHeight, OItemStack[] recipeItems, OItemStack enchantItem, Enchantment result)
	{
		super(recipeWidth, recipeHeight, recipeItems, enchantItem);
		
		if(enchantItem == null)
		{
			throw new IllegalArgumentException("EnchantItem must not be null.");
		}
		if(result == null)
		{
			throw new IllegalArgumentException("Result must not be null.");
		}
		
		RESULT = result;
		
		if(!RESULT.isValid())
		{
			Item item = new Item(enchantItem);
			CraftBookListener.logger.warning("CraftBook Enchant Recipe for item ["+item.getItemId()
											+"] does not have a valid Enchantment ["
											+result.getType().name()+" level#"+result.getLevel()
											+"]. Recipe will not work.");
		}
		
		this.recipeWidth = recipeWidth;
		this.recipeHeight = recipeHeight;
		this.recipeItems = recipeItems;
	}
	
	@Override
	public boolean a(OInventoryCrafting paramOInventoryCrafting, OWorld paramOWorld)
	{
		for (int i = 0; i <= 3 - this.recipeWidth; i++)
		{
			for (int j = 0; j <= 3 - this.recipeHeight; j++)
			{
				if (checkMatch(paramOInventoryCrafting, i, j, true))
					return true;
				if (checkMatch(paramOInventoryCrafting, i, j, false))
					return true;
			}
		}
		return false;
	}
	
	private boolean checkMatch(OInventoryCrafting inventory, int paramInt1, int paramInt2, boolean paramBoolean)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				int k = i - paramInt1;
				int m = j - paramInt2;
				OItemStack recipeItemStack = null;
				if((k >= 0) && (m >= 0) && (k < this.recipeWidth) && (m < this.recipeHeight))
				{
					if(paramBoolean)
						recipeItemStack = this.recipeItems[(this.recipeWidth - k - 1 + m * this.recipeWidth)];
					else
						recipeItemStack = this.recipeItems[(k + m * this.recipeWidth)];
				}
				
				OItemStack invItemStack = inventory.b(i, j);
				if (invItemStack == null && recipeItemStack == null)
				{
					continue;
				}
				
				if ((invItemStack == null && recipeItemStack != null) || (invItemStack != null && recipeItemStack == null))
				{
					return false;
				}
				
				Item recipeItem = new Item(recipeItemStack);
				Item invItem = new Item(invItemStack);
				if(recipeItem.getItemId() != invItem.getItemId())
				{
					BaseItemType baseType = BaseItemType.fromId(recipeItem.getItemId());
					if(baseType == null)
					{
						return false;
					}
					if(!baseType.isSameType(invItem.getItemId()))
					{
						return false;
					}
				}
				if (recipeItem.getDamage() != -1 && recipeItem.getDamage() != invItem.getDamage())
				{
					return false;
				}
				if(recipeItem.getAmount() > invItem.getAmount())
				{
					return false;
				}
				if(recipeItem.getEnchantments() != null && recipeItem.getEnchantments().length > 0)
				{
					if(invItem.getEnchantments() == null || invItem.getEnchantments().length < recipeItem.getEnchantments().length)
					{
						return false;
					}
					
					Enchantment[] invEnchants = new Enchantment[invItem.getEnchantments().length];
					System.arraycopy(invItem.getEnchantments(), 0, invEnchants, 0, invItem.getEnchantments().length);
					
					enchantloop:
					for(Enchantment enchant : recipeItem.getEnchantments())
					{
						for(int ii = 0; ii < invEnchants.length; ii++)
						{
							if(invEnchants[ii] == null)
								continue;
							
							if(invEnchants[ii].getType() == enchant.getType()
								&& invEnchants[ii].getLevel() == enchant.getLevel())
							{
								invEnchants[ii] = null;
								continue enchantloop;
							}
						}
						return false;
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public OItemStack a(OInventoryCrafting oinventory)
	{
		OItemStack oenchantItem = enchantItem();
		Item enchantItem = new Item(oenchantItem);
		BaseItemType baseType = BaseItemType.fromId(enchantItem.getItemId());
		int size = oinventory.k_();
		for(int i = 0; i < size; i++)
		{
			OItemStack oitemstack = oinventory.a(i);
			
			if(oitemstack != null)
			{
				Item item = new Item(oitemstack);
				if( ( (baseType != null && baseType.isSameType(item.getItemId())) || (baseType == null && item.getItemId() == enchantItem.getItemId()) )
					&& (enchantItem.getDamage() == -1 || item.getDamage() == enchantItem.getDamage())
					&& item.getAmount() >= enchantItem.getAmount()
					)
				{
					Enchantment[] enchants = item.getEnchantments();
					if(enchants != null && enchants.length >= MechanismListener.maxEnchantAmount)
						continue; //see if we can find another with an acceptable length
					
					if(!RESULT.isValid())
						return null;
					
					Item resultItem = new Item(item.getItemId(), 1, -1, item.getDamage());
					if(enchants != null)
					{
						for(Enchantment enchant : enchants)
						{
							resultItem.addEnchantment(enchant);
						}
					}
					resultItem.addEnchantment(RESULT);
					return resultItem.getBaseItem();
				}
			}
		}
		
		return null;
	}
	
	public boolean decrStackSizes(OEntityPlayerMP eplayer, OInventoryCrafting paramOInventoryCrafting)
	{
		for (int i = 0; i <= 3 - this.recipeWidth; i++)
		{
			for (int j = 0; j <= 3 - this.recipeHeight; j++)
			{
				if (checkMatch(paramOInventoryCrafting, i, j, true))
				{
					findAndDecrease(eplayer, paramOInventoryCrafting, i, j, true);
					return true;
				}
				if (checkMatch(paramOInventoryCrafting, i, j, false))
				{
					findAndDecrease(eplayer, paramOInventoryCrafting, i, j, false);
					return true;
				}
			}
		}
		return false;
	}
	
	private void findAndDecrease(OEntityPlayerMP eplayer, OInventoryCrafting inventory, int paramInt1, int paramInt2, boolean paramBoolean)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				int k = i - paramInt1;
				int m = j - paramInt2;
				OItemStack recipeItemStack = null;
				if((k >= 0) && (m >= 0) && (k < this.recipeWidth) && (m < this.recipeHeight))
				{
					if(paramBoolean)
						recipeItemStack = this.recipeItems[(this.recipeWidth - k - 1 + m * this.recipeWidth)];
					else
						recipeItemStack = this.recipeItems[(k + m * this.recipeWidth)];
				}
				
				//[TODO]: change if ever figure out a way to quickly get inventoryWidth private value
				// currently hardcoded on the assumption that the value will either be 2 or 3
				// which is BAD!
				int invSize = inventory.k_();
				int invWidth;
				if(invSize == 4)
					invWidth = 2;
				else if(invSize == 9)
					invWidth = 3;
				else
				{
					//Outdated?
					CraftBookListener.logger.warning("CraftBook Enchant Recipe failed to remove items! Outdated?");
					CraftBookListener.logger.warning("You may want to disable Enchantment recipes");
					return;
				}
				
				if(i < 0 || i >= invWidth)
				{
					continue;
				}
				
				int slot = i + j * invWidth;
				
				OItemStack invItemStack = inventory.a(slot);
				if (invItemStack == null && recipeItemStack == null)
				{
					continue;
				}
				
				Item recipeItem = new Item(recipeItemStack);
				
				inventory.a(slot, recipeItem.getAmount());
				
				if(recipeItem.getAmount() > 1)
				{
					invItemStack = inventory.a(slot);
					eplayer.a.b(new OPacket103SetSlot(eplayer.bK.d, slot + 1, invItemStack));
				}
			}
		}
	}
	
	public Enchantment result()
	{
		return RESULT;
	}
	
	public OItemStack enchantItem()
	{
		return b();
	}
}
