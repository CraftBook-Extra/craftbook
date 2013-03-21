
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

	        if (oslot != null && oslot.d()) {
	            OItemStack oitemstack1 = oslot.c();

	            oitemstack = oitemstack1.m();
	            if (i < this.f * 9) {
	                if (!this.a(oitemstack1, this.f * 9, this.c.size(), true)) {
	                    return null;
	                }
	            } else if (!this.a(oitemstack1, 0, this.f * 9, false)) {
	                return null;
	            }

	            if (oitemstack1.a == 0) {
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
	    this.a.f();
	  }

	  public OIInventory e() {
	    return this.a;
	  }
}
