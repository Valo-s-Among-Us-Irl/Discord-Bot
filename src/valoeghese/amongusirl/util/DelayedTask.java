package valoeghese.amongusirl.util;

public final class DelayedTask {
	public DelayedTask(long target, Runnable runnable) {
		this.target = target;
		this.runnable = runnable;
	}

	public final long target;
	public final Runnable runnable;
}
