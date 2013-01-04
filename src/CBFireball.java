


public class CBFireball extends OEntityFireball
{
	private float power = 1.0F;
	
	public CBFireball(OWorld oworld, double x, double y, double z, float rotation, float pitch, float power, double speed)
	{
		super(oworld);
		
		this.power = power;
		
		this.a = new EntityCreatureX(oworld);
        
		a(1.0F, 1.0F);
        
		this.b(x, y, z, 0F, 0F);
		this.b(x, y, z);
        
		this.M = 0.0F;
		this.w = this.x = this.y = 0.0D;
        
		this.b = Math.cos(Math.toRadians(rotation)) * speed;
		this.c = Math.sin(Math.toRadians(pitch)) * speed;
		this.d = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	protected void a(OMovingObjectPosition paramOMovingObjectPosition)
	{
		if (!this.p.I) {
	      if ((paramOMovingObjectPosition.g != null) && 
	        (paramOMovingObjectPosition.g.a(ODamageSource.a(this, this.a), 6)));
	      this.p.a(null, this.t, this.u, this.v, this.power, true, this.p.L().b("mobGriefing"));
	      x();
	    }
	}
}

class EntityCreatureX extends OEntityCreature
{
	public EntityCreatureX(OWorld arg0)
	{
		super(arg0);
	}
	
	@Override
	public int aT()
	{
		return 1;
	}
}
