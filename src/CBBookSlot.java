
public class CBBookSlot extends OSlot
{
	public CBBookSlot(OIInventory arg0, int arg1, int arg2, int arg3)
	{
		super(arg0, arg1, arg2, arg3);
	}
	
	@Override
	//isItemValid
	public boolean a(OItemStack ostack)
	{
		return ostack.c == 340
				|| ostack.c == 386
				|| ostack.c == 387;
	}
}
