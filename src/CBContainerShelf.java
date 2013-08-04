
public class CBContainerShelf extends OContainer
{
	private OIInventory a;
	private int f;
	
	public CBContainerShelf(OIInventory oiinventory, OIInventory oiinventory1)
	{
		this.a = oiinventory1;
        this.f = oiinventory1.j_() / 9;
        
        int i = (this.f - 4) * 18;

        int j;
        int k;

        for (j = 0; j < this.f; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new CBBookSlot(oiinventory1, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new OSlot(oiinventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.a(new OSlot(oiinventory, j, 8 + j * 18, 161 + i));
        }
	}
	
	
	//[NOTE] below is a complete copy of OContainerChest
	
	
	  public boolean a(OEntityPlayer paramOEntityPlayer)
	  {
	    return this.a.a(paramOEntityPlayer);
	  }

	  public OItemStack b(OEntityPlayer oentityplayer, int i)
	  {
	        OItemStack oitemstack = null;
	        OSlot oslot = (OSlot) this.c.get(i);
	        //Notchian: Slot.getHasStack, func_75216_d
	        if (oslot != null && oslot.e()) {
	        	// Notchian: Slot.getStack, func_75211_c
	            OItemStack oitemstack1 = oslot.d();

	            oitemstack = oitemstack1.m();
	            if (i < this.f * 9) {
	                if (!this.a(oitemstack1, this.f * 9, this.c.size(), true)) {
	                    return null;
	                }
	            } else if (!this.a(oitemstack1, 0, this.f * 9, false)) {
	                return null;
	            }

	            if (oitemstack1.b == 0) {
	                oslot.c((OItemStack) null);
	            } else {
	                oslot.e();
	            }
	        }

	        return oitemstack;

	  }

	  public void b(OEntityPlayer paramOEntityPlayer)
	  {
	    super.b(paramOEntityPlayer);
	    //Notchian: IInventory.openChest, func_70295_k_
	    this.a.k_();
	  }

	  public OIInventory e() {
	    return this.a;
	  }
}
