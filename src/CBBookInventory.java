import com.sk89q.craftbook.CraftBookWorld;


public class CBBookInventory implements OIInventory
{
	private OItemStack[] items = new OItemStack[36];
	public boolean locked = false;
	
	public CraftBookWorld cbworld;
	public int x;
	public int y;
	public int z;
	
	@Override
	//getSizeInventory func_70302_i_
	public int j_()
	{
		return 27;
	}
	
	@Override
	//getStackInSlot func_70301_a
	public OItemStack a(int slot)
	{
		return items[slot];
	}

	@Override
	//isUseableByPlayer
	public boolean a(OEntityPlayer oplayer)
	{
		return !locked;
	}

	@Override
	//decrStackSize
	public OItemStack a(int slot, int amount)
	{
		if (this.items[slot] != null)
        {
            OItemStack stack;

            if (this.items[slot].a <= amount)
            {
            	stack = this.items[slot];
                this.items[slot] = null;
                return stack;
            }
            else
            {
            	stack = this.items[slot].a(amount);

                if (this.items[slot].a == 0)
                {
                    this.items[slot] = null;
                }

                return stack;
            }
        }
        else
        {
            return null;
        }
	}

	@Override
	//setInventorySlotContents
	public void a(int slot, OItemStack stack)
	{
		this.items[slot] = stack;

        if (stack != null && stack.a > this.d())
        {
        	stack.a = this.d();
        }
	}

	@Override
	//getStackInSlotOnClosing
	public OItemStack b(int slot)
	{
		if (this.items[slot] != null)
        {
            OItemStack stack = this.items[slot];
            this.items[slot] = null;
            return stack;
        }
        else
        {
            return null;
        }
	}

	@Override
	//getInvName
	public String b()
	{
		return "container.chest";
	}

	@Override
	//getInventoryStackLimit func_70297_j_
	public int d()
	{
		return 64;
	}

	@Override
	//onInventoryChanged func_70296_d
	public void k_()
	{
		CraftBook.cbdata.markDirty();
	}
	
	@Override
	//openChest func_70295_k_
	public void f()
	{
		
	}
	
	@Override
	//closeChest
	public void g()
	{
		
	}
	
	public void readFromNBT(ONBTTagCompound nbtcompound)
    {
		String name = nbtcompound.i("world");
		int dim = nbtcompound.e("dimension");
		cbworld = new CraftBookWorld(name, dim);
		x = nbtcompound.e("x");
		y = nbtcompound.e("y");
		z = nbtcompound.e("z");
		
		ONBTTagList localONBTTagList = nbtcompound.m("Items");
		
        int var2;

        for (var2 = 0; var2 < this.j_(); ++var2)
        {
            this.a(var2, (OItemStack)null);
        }

        for (var2 = 0; var2 < localONBTTagList.c(); ++var2)
        {
            ONBTTagCompound var3 = (ONBTTagCompound)localONBTTagList.b(var2);
            int var4 = var3.c("Slot") & 255;

            if (var4 >= 0 && var4 < this.j_())
            {
                this.a(var4, OItemStack.a(var3));
            }
        }
    }

    public void writeToNBT(ONBTTagCompound nbtcompound)
    {
    	nbtcompound.a("world", cbworld.name());
    	nbtcompound.a("dimension", cbworld.dimension());
    	nbtcompound.a("x", x);
    	nbtcompound.a("y", y);
    	nbtcompound.a("z", z);
    	
        ONBTTagList var1 = new ONBTTagList();

        for (int var2 = 0; var2 < this.j_(); ++var2)
        {
            OItemStack var3 = this.a(var2);

            if (var3 != null)
            {
                ONBTTagCompound var4 = new ONBTTagCompound();
                var4.a("Slot", (byte)var2);
                var3.b(var4);
                var1.a(var4);
            }
        }

        nbtcompound.a("Items", var1);
    }

	@Override
	public boolean b(int arg0, OItemStack arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean c() {
		// TODO Auto-generated method stub
		return false;
	}
}
