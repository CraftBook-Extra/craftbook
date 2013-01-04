import java.util.List;


public class CBSlotCrafting extends OSlotCrafting
{
	private final OIInventory craftMatrix;
	private OEntityPlayer thePlayer;
	
	public CBSlotCrafting(OEntityPlayer eplayer, OIInventory craftMatrix,
							OIInventory arg2, int arg3, int arg4, int arg5)
	{
		super(eplayer, craftMatrix, arg2, arg3, arg4, arg5);
		
		this.craftMatrix = craftMatrix;
		this.thePlayer = eplayer;
	}
	
	@Override
	public void b(OItemStack paramOItemStack)
	{
		if(!(this.craftMatrix instanceof OInventoryCrafting))
		{
			super.b(paramOItemStack);
			return;
		}
		
		OInventoryCrafting oinvcrafting = (OInventoryCrafting)this.craftMatrix;
		
		@SuppressWarnings("rawtypes")
		List recipes = OCraftingManager.a().b();
		
		CBEnchantRecipe cbrecipe = null;
		for(int i = 0; i < recipes.size(); i++)
		{
			OIRecipe oirecipe = (OIRecipe)recipes.get(i);
			if(oirecipe.a(oinvcrafting, thePlayer.p))
			{
				if(!(oirecipe instanceof CBEnchantRecipe))
				{
					super.b(paramOItemStack);
					return;
				}
				
				cbrecipe = (CBEnchantRecipe)oirecipe;
				break;
			}
		}
		
		if(cbrecipe == null)
		{
			super.b(paramOItemStack);
			return;
		}
		
		c(paramOItemStack);
		
		cbrecipe.decrStackSizes((OEntityPlayerMP)thePlayer, oinvcrafting);
		
		for(int i = 0; i < this.craftMatrix.k_(); i++)
		{
			OItemStack oitemstack = this.craftMatrix.a(i);
			
			if (oitemstack == null)
            {
                continue;
            }

            if (!oitemstack.b().s())
            {
                continue;
            }

            OItemStack oitemstack1 = new OItemStack(oitemstack.b().r());

            if (oitemstack.b().j(oitemstack) && thePlayer.bJ.a(oitemstack1))
            {
                continue;
            }

            if (craftMatrix.a(i) == null)
            {
                craftMatrix.a(i, oitemstack1);
            }
            else
            {
                thePlayer.c(oitemstack1);
            }
		}
	}
}
