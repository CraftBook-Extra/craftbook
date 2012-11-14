
public class CBContainerShelf extends OContainer
{
	private OIInventory e;
	private int f;
	
	public CBContainerShelf(OIInventory oiinventory, OIInventory oiinventory1)
	{
		this.e = oiinventory1;
        this.f = oiinventory1.k_() / 9;
        oiinventory1.l_();
        
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
	
	
	public boolean c(OEntityPlayer paramOEntityPlayer)
	{
		return this.e.a(paramOEntityPlayer);
	}

	public OItemStack b(OEntityPlayer paramOEntityPlayer, int paramInt)
	{
		OItemStack localOItemStack1 = null;
		OSlot localOSlot = (OSlot)this.b.get(paramInt);
		if ((localOSlot != null) && (localOSlot.d())) {
			OItemStack localOItemStack2 = localOSlot.c();
			localOItemStack1 = localOItemStack2.l();
		
			if (paramInt < this.f * 9) {
				if (!a(localOItemStack2, this.f * 9, this.b.size(), true)) {
					return null;
				}
			}
			else if (!a(localOItemStack2, 0, this.f * 9, false)) {
				return null;
			}
		
			if (localOItemStack2.a == 0)
				localOSlot.c(null);
			else {
				localOSlot.e();
			}
		}
		return localOItemStack1;
	}

	public void a(OEntityPlayer paramOEntityPlayer)
	{
		super.a(paramOEntityPlayer);
		this.e.f();
	}
}
