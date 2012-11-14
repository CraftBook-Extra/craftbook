import java.util.concurrent.LinkedBlockingQueue;

import com.sk89q.craftbook.WorldBlockVector;


public class OutputLever implements Runnable
{
	private final LinkedBlockingQueue<WorldBlockVector> outputQueue = new LinkedBlockingQueue<WorldBlockVector>();
	
	protected void addToOutputQueue(WorldBlockVector outputBlock)
	{
		try
		{
			outputQueue.put(outputBlock);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
    {
    	for(WorldBlockVector output = outputQueue.poll(); output != null; output = outputQueue.poll())
    	{
    		OBlock.aM.a(CraftBook.getOWorldServer(output.getCBWorld()), output.getBlockX(), output.getBlockY(), output.getBlockZ(), (OEntityPlayer)null, 0, 0.0F, 0.0F, 0.0F);
    	}
    }
}
