package aQute.timer;

import java.util.*;

import aQute.bnd.annotation.component.*;


@Component(provide=java.util.Timer.class, servicefactory=true)
public class Timer extends java.util.Timer {
	final List<TimerTask> tasks = new ArrayList<TimerTask>();
	
	
	@Deactivate
	void deactivate() {
		
	}

	
	class Wrapper extends TimerTask {
		TimerTask task;
		
		Wrapper(TimerTask t) {
		}

		@Override
		public void run() {
			try {
				task.run();
			} finally {
				tasks.remove(this);
			}
		}
		
		@Override
		public boolean cancel(){
			tasks.remove(this);
			return super.cancel();
		}		
	}
	
	
	@Override
	public void schedule(TimerTask t, long delay, long period) {
		super.schedule(wrap(t), delay, period);
	}
	@Override
	public void schedule(TimerTask t, Date date) {
		super.schedule(wrap(t), date);
	}
	
	@Override
	public void schedule(TimerTask t, Date date, long period) {
		super.schedule(wrap(t), date, period);
	}

	@Override
	public void schedule(TimerTask t,long delay) {
		super.schedule(wrap(t), delay);
	}
	
	@Override
	public void scheduleAtFixedRate(TimerTask t, Date date, long period) {
		super.scheduleAtFixedRate(wrap(t), date, period);		
	}

	@Override
	public void scheduleAtFixedRate(TimerTask t, long delay, long period) {
		super.scheduleAtFixedRate(wrap(t), delay, period);
	}

	private TimerTask wrap(TimerTask t) {
		Wrapper w = new Wrapper(t);
		tasks.add(w);
		return w;
	}
	
}
