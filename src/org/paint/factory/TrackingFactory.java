package org.paint.factory;

import org.bbop.framework.AbstractComponentFactory;
import org.paint.gui.tracking.EvidencePanel;

public class TrackingFactory extends AbstractComponentFactory<EvidencePanel> {

		public FactoryCategory getCategory() {
			return FactoryCategory.VIEWERS;
		}

		public String getName() {
			return "Evidence";
		}

		public boolean isSingleton() {
			return true;
		}

		public String getID() {
			return "evidence";
		}

		@Override
		public EvidencePanel doCreateComponent(String id) {
			return EvidencePanel.inst();
		}
}

