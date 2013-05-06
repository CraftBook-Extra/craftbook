


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
        //field_70129_M
		this.N = 0.0F;
		//field_70159_w = this. field_70181_x = field_70179_y
		this.x = this.y = this.z = 0.0D;
        
		this.b = Math.cos(Math.toRadians(rotation)) * speed;
		this.c = Math.sin(Math.toRadians(pitch)) * speed;
		this.d = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	protected void a(OMovingObjectPosition paramOMovingObjectPosition)
	{
		//this. field_70170_p .I
		if (!this.q.I) {
	      if ((paramOMovingObjectPosition.g != null) && 
	        (paramOMovingObjectPosition.g.a(ODamageSource.a(this, this.a), 6)));
	      //newExplosion
	      //this.field_70170_p.a(field_70165_t, field_70163_u, field_70161_v, .., .., this.field_70170_p.func_82736_K. b("")
	      this.q.a(null, this.u, this.v, this.w, this.power, true, this.q.N().b("mobGriefing"));
	      //setDead func_70106_y
	      w();
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
	public int aW()
	{
		return 1;
	}
}
