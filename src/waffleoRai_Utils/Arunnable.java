package waffleoRai_Utils;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Arunnable implements Runnable{

	private volatile boolean killRequest;
	
	private volatile boolean paused;
	private volatile boolean pauseRequest;
	private volatile boolean unpauseRequest;
	
	protected boolean sleeps; //If not, then it waits until interrupted.
	
	protected long sleeptime;
	protected long delay;
	
	protected BlockingQueue<Thread> myThreads;
	
	protected String name;
	
	private volatile int skipWaitCycles;
	
	protected void setRandomName()
	{
		Random r = new Random();
		name = "Runner_" + Integer.toHexString(r.nextInt());
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String n)
	{
		name = n;
	}
	
	private void instantiateThreadList()
	{
		myThreads = new LinkedBlockingQueue<Thread>();
	}
	
	public void run()
	{
		if(myThreads == null) instantiateThreadList();
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.run || " + name + " || DEBUG: Run Called!");
		myThreads.add(Thread.currentThread());
		//Wait for delay time unless interrupted.
		try 
		{
			Thread.sleep(delay);
		} 
		catch (InterruptedException e) 
		{
			//System.err.println(Thread.currentThread().getName() + " || Arunnable.run || Initial delay interrupted. Run will now commence.");
			//e.printStackTrace();
		}
		
		if(sleeps) 
		{
			if(sleeptime > 0) runWithSleep();
			else runContinuously();
		}
		else runWithWait();
		myThreads.remove(Thread.currentThread());
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.run || " + name + " || DEBUG: Run returning...");
	}
	
	protected void pauseBlock()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.pauseBlock || " + name + " || DEBUG: Called!");
		if (unpauseRequest)
		{
			//System.err.println(Thread.currentThread().getName() + " || Arunnable.pauseBlock || " + name + " || DEBUG: Processing resume request...");
			paused = false;
			unpauseRequest = false;
		}
		else
		{
			//Block
			try 
			{
				//synchronized(this) {wait();}
				//System.err.println(Thread.currentThread().getName() + " || Arunnable.pauseBlock || " + name + " || DEBUG: Starting wait...");
				//wait();
				synchronized(this) {wait();}
			} 
			catch (InterruptedException e) 
			{
				//Breaks pause to check for unpause or kill or something else...
				//System.err.println(Thread.currentThread().getName() + " || Arunnable.pauseBlock || " + name + " || DEBUG: Wait broken!");
				//e.printStackTrace();
			}
		}
	}
	
	private void runWithWait()
	{
		while(!killRequest)
		{
			//Check pausing
			if (paused)
			{
				pauseBlock();
			}
			else
			{
				if(pauseRequest)
				{
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithWait || " + name + " || DEBUG: Processing pause request...");
					paused = true;
					pauseRequest = false;
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithWait || " + name + " || DEBUG: Pause request processed");
					continue;
				}
				//Do the thing
				doSomething();
				//Block until next interruption
				if(skipWaitCycles <= 0)
				{
					try 
					{
						synchronized(this) {wait();}
						//wait();
					} 
					catch (InterruptedException e) 
					{
						//Breaks wait
						//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithWait || " + name + " || DEBUG: Wait interrupted! Resuming run...");
						//e.printStackTrace();
					}	
				}
				else {
					skipWaitCycles--;
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithWait || " + name + " || DEBUG: Wait skipped! Remaining skips: " + skipWaitCycles);
				}
			}
			
		}	
	}
	
	private void runWithSleep()
	{
		while(!killRequest)
		{
			//Check pausing
			if (paused)
			{
				pauseBlock();
			}
			else
			{
				if(pauseRequest)
				{
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithSleep || " + name + " || DEBUG: Processing pause request...");
					paused = true;
					pauseRequest = false;
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithSleep || " + name + " || DEBUG: Pause request processed!");
					continue;
				}
				//Do the thing
				doSomething();
				//Block until next interruption or sleep expires
				if (skipWaitCycles <= 0)
				{
					try 
					{
						synchronized(this) {Thread.sleep(sleeptime);}
						//Thread.sleep(sleeptime);
					} 
					catch (InterruptedException e) 
					{
						//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithSleep || " + name + " || DEBUG: Sleep interrupted!");
						//Just keep going
						//e.printStackTrace();
					}	
				}
				else
				{
					skipWaitCycles--;	
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithSleep || " + name + " || DEBUG: Sleep skipped! Remaining skips: " + skipWaitCycles);
				}
			}
			
		}	
	}
	
	private void runContinuously()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.runContinuously || " + name + " || DEBUG: Called!");
		while(!killRequest)
		{
			//Check pausing
			if (paused)
			{
				pauseBlock();
			}
			else
			{
				if(pauseRequest)
				{
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runContinuously || " + name + " || DEBUG: Processing pause request...");
					paused = true;
					pauseRequest = false;
					//System.err.println(Thread.currentThread().getName() + " || Arunnable.runContinuously || " + name + " || DEBUG: Pause request processed!");
					continue;
				}
				//Do the thing
				doSomething();
				//No blocking; it will immediately check the while condition again.
			}
			
		}	
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestContinuously || " + name + " || DEBUG: Returning...");
	}
	
	public abstract void doSomething();
	
	public void requestTermination()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestTermination || " + name + " || DEBUG: Requesting Termination...");
		killRequest = true;
		synchronized(this)
		{for(Thread t : myThreads) t.interrupt();}
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestTermination || " + name + " || DEBUG: Termination request sent!");
	}
	
	public void requestPause()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestPause || " + name + " || DEBUG: Requesting Pause...");
		pauseRequest = true;
		synchronized(this)
		{for(Thread t : myThreads) t.interrupt();}
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestPause || " + name + " || DEBUG: Pause request sent!");
	}
	
	public void requestResume()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestResume || " + name + " || DEBUG: Requesting Resume...");
		unpauseRequest = true;
		synchronized(this)
		{for(Thread t : myThreads) t.interrupt();}
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.requestResume || " + name + " || DEBUG: Resume request sent!");
	}
	
	public boolean isPaused()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.isPaused || " + name + " || DEBUG: Checking pause...");
		return paused;
	}
	
	public void interruptThreads()
	{
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.interruptThreads || " + name + " || DEBUG: Interrupt Requested...");
		synchronized(this)
		{
			for(Thread t : myThreads) 
			{
			//System.err.println(Thread.currentThread().getName() + " || Arunnable.interruptThreads || " + name + " || DEBUG: Interrupting thread " + t.getName());
				t.interrupt();	
			}
		}
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.interruptThreads || " + name + " || DEBUG: Interrupt Attempts Complete!");
	}
	
	public boolean killRequested()
	{
		return this.killRequest;
	}
	
	public boolean pauseRequested()
	{
		return this.pauseRequest;
	}
	
	protected void addWaitSkip(int n)
	{
		if (n < 0) return;
		skipWaitCycles += n;
		//System.err.println(Thread.currentThread().getName() + " || Arunnable.runWithSleep || " + name + " || DEBUG: Wait skip added! Skips left: " + skipWaitCycles);
	}
	
	public boolean anyThreadsAlive()
	{
		return !myThreads.isEmpty();
	}
	
}
