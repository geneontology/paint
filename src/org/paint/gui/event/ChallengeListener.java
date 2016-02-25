package org.paint.gui.event;

import java.util.EventListener;

public interface ChallengeListener extends EventListener {

	public void handleChallengeEvent(ChallengeEvent event);
	
}
