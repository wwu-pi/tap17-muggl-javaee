package de.wwu.muggl.ui.gui.support;

import org.apache.log4j.spi.LoggingEvent;

import de.wwu.muggl.ui.gui.components.StepByStepExecutionComposite;

/**
 * Log4j Logging Appender to be used in the StepByStepExecutionWindow. It will forward
 * the time, the level and the message to the window so it can be displayed there. It
 * extends the AppenderSkeleton of the org.apache.log4j package.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-10-14
 */
public class StepByStepLoggingAppender extends org.apache.log4j.AppenderSkeleton {
	private StepByStepExecutionComposite parent;

	/**
	 * Basic constructor.
	 * @param parent The parent StepByStepExecutionComposite.
	 */
	public StepByStepLoggingAppender(StepByStepExecutionComposite parent) {
		this.parent = parent;
	}

	/**
	 * Overrides the super implementation and just forwards
	 * the time, the level and the message to the StepByStepExecutionComposite.
	 * @param event The LoggingEvent.
	 */
	@Override
	public void append(LoggingEvent event) {
		// Write the new entry.
		this.parent.appendLoggingStyledText(event.timeStamp - LoggingEvent.getStartTime(), event.getLevel(), event.getMessage().toString());
	}

	/**
	 * Closing of this appender is not necessary. When closing the StepByStepExecutionComposite
	 * the Appender will be removed from the active appenders list of the logger.
	 */
	@Override
	public void close() {
		// Do nothing
	}

	/**
	 * Return false, the layout is determined by the StepByStepExecutionComposite.
	 * @return False, since no Layout is required.
	 */
	@Override
	public boolean requiresLayout() {
		return false;
	}


}
