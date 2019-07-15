package waffleoRai_Utils;

public abstract class Athread extends Thread
{
	private boolean killRequest;
	
	private boolean paused;
	private boolean pauseRequest;
	private boolean unpauseRequest;
	
	private long sleeptime;
	private long delay;
	
	public synchronized void setDefaults()
	{
		killRequest = false;
		paused = false;
		pauseRequest = false;
		unpauseRequest = false;
		sleeptime = 1000;
		delay = 0;
	}
	
	public void run()
	{
		//Initial delay
		try 
		{
			Thread.sleep(delay);
		} 
		catch (InterruptedException e) 
		{
			Thread.interrupted();
		}
		
		//While not killed...
		while (!killRequested())
		{
			//Reset interrupt flag
			Thread.interrupted();
			//Check for pauses
			if (isPaused())
			{
				//Check for unpause request
				if (unpauseRequested()) setPause(false);
			}
			else
			{
				//Check for pause request
				if (pauseRequested()) setPause(true);
			}
			
			//Execute the doSomething function only if not paused now
			if (!isPaused())
			{
				try
				{
					doSomething();
				}
				catch (Exception e)
				{
					System.err.println("Athread.run || Exception caught by thread " + Thread.currentThread().getName() + ": Thread was not terminated.");
					e.printStackTrace();
				}
			}
			
			//Sleep
			try 
			{
				Thread.sleep(sleeptime);
			} 
			catch (InterruptedException e) 
			{
				Thread.interrupted();
			}
			
		}
	}
	
	public abstract void doSomething();
	
	protected synchronized boolean killRequested()
	{
		return killRequest;
	}
	
	public synchronized boolean isPaused()
	{
		return paused;
	}
	
	protected synchronized boolean pauseRequested()
	{
		return pauseRequest;
	}
	
	protected synchronized boolean unpauseRequested()
	{
		return unpauseRequest;
	}
	
	public synchronized void kill()
	{
		killRequest = true;
		this.interrupt();
	}
	
	public synchronized void interruptMe()
	{
		this.interrupt();
	}
	
	public synchronized void pause()
	{
		pauseRequest = true;
		this.interrupt();
	}
	
	public synchronized void unpause()
	{
		unpauseRequest = true;
		this.interrupt();
	}

	public synchronized void setSleeptime_millis(long millis)
	{
		sleeptime = millis;
	}
	
	public synchronized void setInitialDelay_millis(long millis)
	{
		delay = millis;
	}
	
	private synchronized void setPause(boolean p)
	{
		paused = p;
		if (!p) unpauseRequest = false;
		else pauseRequest = false;
	}
	
}
