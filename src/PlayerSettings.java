import java.util.HashMap;


public class PlayerSettings
{
	@SuppressWarnings("rawtypes")
	protected HashMap activePotionsMap = null;
	protected int currentWindowId = 0;
	
	protected void getNextWindowId()
	{
		this.currentWindowId = this.currentWindowId % 26 + 101;
	}
}
