package toremove.org.eclipse.lsp4e.operations.folding;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class LSPFoldingReconciler extends MonoReconciler {

	public LSPFoldingReconciler() {
		super(new LSPFoldingReconcilingStrategy(), false);
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		if (textViewer instanceof ProjectionViewer) {
			((LSPFoldingReconcilingStrategy) getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE))
					.install((ProjectionViewer) textViewer);
		}
	}

	@Override
	public void uninstall() {
		super.uninstall();
		((LSPFoldingReconcilingStrategy) getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE)).uninstall();
	}

}
