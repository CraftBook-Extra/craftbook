import java.util.Map;


public class PlayerSettings
{
	@SuppressWarnings("rawtypes")
	//protected HashMap activePotionsMap = null;
	protected Map activePotionsMap = null;
	protected int currentWindowId = 0;
	
	protected void getNextWindowId()
	{
		this.currentWindowId = this.currentWindowId % 26 + 101;
	}
}
