
public class CBData extends OWorldSavedData
{
	public CBData(String arg0)
	{
		super(arg0);
	}
	
	public CBData()
	{
		super("craftbook");
		markDirty();
	}
	
	public void markDirty()
	{
		c();
	}
	
	@Override
	public void a(ONBTTagCompound nbtcompound)
	{
		MechanismListener.readFromNBT(nbtcompound);
	}
	
	@Override
	public void b(ONBTTagCompound nbtcompound)
	{
		MechanismListener.writeToNBT(nbtcompound);
	}
}
