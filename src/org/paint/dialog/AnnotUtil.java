package org.paint.dialog;

import java.util.List;

import org.bbop.framework.GUIManager;
import org.bbop.phylo.annotate.PaintAction;
import org.bbop.phylo.annotate.WithEvidence;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.Tree;
import org.bbop.phylo.util.TaxonChecker;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.main.PaintManager;

public class AnnotUtil {

	private static AnnotUtil INSTANCE;

	private AnnotUtil() {
	}

	public static AnnotUtil inst() {
		if (INSTANCE == null) {
			INSTANCE = new AnnotUtil();
		}
		return INSTANCE;
	}

	public void propagateAssociation(Bioentity node, Tree tree, String term, String date) {
		boolean valid_for_all_descendents = TaxonChecker.checkTaxons(tree, node, term, false);
		if (!valid_for_all_descendents) {
			List<String> invalid_taxa = TaxonChecker.getInvalidTaxa(node, term);
			TaxonDialog taxon_dialog = new TaxonDialog(GUIManager.getManager().getFrame(), term, invalid_taxa);
			valid_for_all_descendents = taxon_dialog.isLost();
		}
		if (valid_for_all_descendents) {
			WithEvidence withs = new WithEvidence(tree, node, term);
			int qualifiers = withs.getWithQualifiers();
			if (qualifiers > 0) {
				QualifierDialog qual_dialog = new QualifierDialog(GUIManager.getManager().getFrame(), qualifiers);
				qualifiers = qual_dialog.getQualifiers();
			}
			PaintAction.inst().propagateAssociation(PaintManager.inst().getFamily(), node, term, withs, date, qualifiers);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
		}
	}
}