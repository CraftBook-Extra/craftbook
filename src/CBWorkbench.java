import java.util.List;


public class CBWorkbench extends OBlockWorkbench
{

	protected CBWorkbench(int arg0)
	{
		super(arg0);
		c(2.5F);
		a(OBlock.g);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean a(OWorld oworld, int x, int y, int z, OEntityPlayer eplayer, int direction, float offsetx, float offsety, float offsetz)
	{
		boolean output = super.a(oworld, x, y, z, eplayer, direction, offsetx, offsety, offsetz);
		// Notchian: EntityPlayer.inventoryContainer, field_71069_bz
		if(!(eplayer.bo instanceof OContainerWorkbench))
			return output;
		
		OContainerWorkbench ocontainerwb = (OContainerWorkbench)eplayer.bo;
		
		@SuppressWarnings("rawtypes")
		List inventorySlots = ocontainerwb.b;
		
		if(inventorySlots != null)
		{
			for(int i = 0; i < inventorySlots.size(); i++)
			{
				if(inventorySlots.get(i) instanceof OSlotCrafting)
				{
					CBSlotCrafting cbslot = new CBSlotCrafting(eplayer, ocontainerwb.a, ocontainerwb.f, 0, 124, 35);
					inventorySlots.set(i, cbslot);
					
					return output;
				}
			}
		}
		
		return output;
	}
}
