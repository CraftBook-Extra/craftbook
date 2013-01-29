import java.util.concurrent.LinkedBlockingQueue;

import com.sk89q.craftbook.WorldBlockVector;


public class OutputLever implements Runnable 
{
	private final LinkedBlockingQueue<WorldBlockVector> outputQueue = new LinkedBlockingQueue<WorldBlockVector>();
	
	protected void addToOutputQueue(WorldBlockVector outputBlock) {
		try {
			outputQueue.put(outputBlock);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		//toggle all levers in the outputQueue with a simulated right-click
    	for(WorldBlockVector output = outputQueue.poll(); output != null; output = outputQueue.poll()) {
    		CraftBook.getWorld(output.getCBWorld())
    				 .getBlockAt(output.getBlockX(), output.getBlockY(), output.getBlockZ())
    				 .rightClick(null);    		
    	}
    }
}
