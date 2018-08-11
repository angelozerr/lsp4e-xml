package toremove.org.eclipse.lsp4e.operations.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import toremove.org.eclipse.lsp4j.FoldingRange;
import toremove.org.eclipse.lsp4j.FoldingRangeRequestParams;

public class LSPFoldingReconcilingStrategy
		implements IReconcilingStrategy, IReconcilingStrategyExtension, IProjectionListener {

	private IDocument document;
	private ProjectionAnnotationModel projectionAnnotationModel;
	private List<LSPDocumentInfo> infos;
	private CompletableFuture<List<FoldingRange>> request;
	private ProjectionViewer viewer;

	/**
	 * A FoldingAnnotation is a {@link ProjectionAnnotation} it is folding and
	 * overriding the paint method (in a hacky type way) to prevent one line folding
	 * annotations to be drawn.
	 */
	protected class FoldingAnnotation extends ProjectionAnnotation {
		private boolean visible; /* workaround for BUG85874 */

		/**
		 * Creates a new FoldingAnnotation.
		 * 
		 * @param isCollapsed true if this annotation should be collapsed, false
		 *                    otherwise
		 */
		public FoldingAnnotation(boolean isCollapsed) {
			super(isCollapsed);
			visible = false;
		}

		/**
		 * Does not paint hidden annotations. Annotations are hidden when they only span
		 * one line.
		 * 
		 * @see ProjectionAnnotation#paint(org.eclipse.swt.graphics.GC,
		 *      org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		@Override
		public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
			/* workaround for BUG85874 */
			/*
			 * only need to check annotations that are expanded because hidden annotations
			 * should never have been given the chance to collapse.
			 */
			if (!isCollapsed()) {
				// working with rectangle, so line height
				FontMetrics metrics = gc.getFontMetrics();
				if (metrics != null) {
					// do not draw annotations that only span one line and
					// mark them as not visible
					if ((rectangle.height / metrics.getHeight()) <= 1) {
						visible = false;
						return;
					}
				}
			}
			visible = true;
			super.paint(gc, canvas, rectangle);
		}

		@Override
		public void markCollapsed() {
			/* workaround for BUG85874 */
			// do not mark collapsed if annotation is not visible
			if (visible)
				super.markCollapsed();
		}
	}

	@Override
	public void reconcile(IRegion subRegion) {
		if (projectionAnnotationModel == null) {
			return;
		}

		if (infos == null) {
			infos = LanguageServiceAccessor.getLSPDocumentInfosFor(document,
					capabilities -> Boolean.TRUE.equals(capabilities.getDocumentHighlightProvider()));
		}
		if (infos.isEmpty()) {
			// The language server has not the highlight capability.
			return;
		}
		// Cancel the last call of 'foldingRanges'.
		cancel();
		for (LSPDocumentInfo info : infos) {
			TextDocumentIdentifier identifier = new TextDocumentIdentifier(info.getFileUri().toString());
			FoldingRangeRequestParams params = new FoldingRangeRequestParams(identifier);

			request = info.getInitializedLanguageClient().thenCompose(languageServer -> {
				if (languageServer instanceof XMLLanguageServerInterface) {
					return ((XMLLanguageServerInterface) languageServer).foldingRanges(params);
				}
				return null;
			});
			request.thenAccept(result -> {

				// these are what are passed off to the annotation model to
				// actually create and maintain the annotations
				List<Annotation> modifications = new ArrayList<Annotation>();
				List<FoldingAnnotation> deletions = new ArrayList<FoldingAnnotation>();
				List<FoldingAnnotation> existing = new ArrayList<FoldingAnnotation>();
				Map<Annotation, Position> additions = new HashMap<Annotation, Position>();

				// find and mark all folding annotations with length 0 for deletion
				markInvalidAnnotationsForDeletion(deletions, existing);

				try {
					if (result != null) {
						Collections.sort(result, (f1, f2) -> f2.getEndLine() - f1.getEndLine());
						for (FoldingRange foldingRange : result) {
							updateAnnotation(modifications, deletions, existing, additions, foldingRange.getStartLine(),
									foldingRange.getEndLine());
						}
					}
				} catch (BadLocationException e) {
					// should never occur
				}

				// be sure projection has not been disabled
				if (projectionAnnotationModel != null) {
					if (existing.size() > 0) {
						deletions.addAll(existing);
					}
					// send the calculated updates to the annotations to the
					// annotation model
					projectionAnnotationModel.modifyAnnotations(deletions.toArray(new Annotation[1]), additions,
							modifications.toArray(new Annotation[0]));
				}
			});
		}
	}

	/**
	 * Cancel the last call of 'foldingRanges'.
	 */
	private void cancel() {
		if (request != null && !request.isDone()) {
			request.cancel(true);
			request = null;
		}
	}

	public void install(ProjectionViewer viewer) {
		if (this.viewer != null) {
			this.viewer.removeProjectionListener(this);
		}
		this.viewer = viewer;
		this.viewer.addProjectionListener(this);
		this.projectionAnnotationModel = this.viewer.getProjectionAnnotationModel();
	}

	public void uninstall() {
		setDocument(null);
		if (viewer != null) {
			viewer.removeProjectionListener(this);
			viewer = null;
		}
		projectionDisabled();
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void projectionDisabled() {
		projectionAnnotationModel = null;
	}

	@Override
	public void projectionEnabled() {
		if (viewer != null) {
			projectionAnnotationModel = viewer.getProjectionAnnotationModel();
		}
	}

	/**
	 * Update annotations.
	 * 
	 * @param modifications the folding annotations to update.
	 * @param deletions     the folding annotations to delete.
	 * @param existing      the existing folding annotations.
	 * @param additions     annoation to add
	 * @param line          the line index
	 * @param endLineNumber the end line number
	 * @throws BadLocationException
	 */
	private void updateAnnotation(List<Annotation> modifications, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing, Map<Annotation, Position> additions, int line, Integer endLineNumber)
			throws BadLocationException {
		int startOffset = document.getLineOffset(line);
		int endOffset = document.getLineOffset(endLineNumber) + document.getLineLength(endLineNumber);
		Position newPos = new Position(startOffset, endOffset - startOffset);
		if (existing.size() > 0) {
			FoldingAnnotation existingAnnotation = existing.remove(existing.size() - 1);
			updateAnnotations(existingAnnotation, newPos, modifications, deletions);
		} else {
			additions.put(new FoldingAnnotation(false), newPos);
		}
	}

	/**
	 * Update annotations.
	 * 
	 * @param existingAnnotation the existing annotations that need to be updated
	 *                           based on the given dirtied IndexRegion
	 * @param newPos             the new position that caused the annotations need
	 *                           for updating and null otherwise.
	 * @param modifications      the list of annotations to be modified
	 * @param deletions          the list of annotations to be deleted
	 */
	protected void updateAnnotations(Annotation existingAnnotation, Position newPos, List<Annotation> modifications,
			List<FoldingAnnotation> deletions) {
		if (existingAnnotation instanceof FoldingAnnotation) {
			FoldingAnnotation foldingAnnotation = (FoldingAnnotation) existingAnnotation;

			// if a new position can be calculated then update the position of
			// the annotation,
			// else the annotation needs to be deleted
			if (newPos != null && newPos.length > 0 && projectionAnnotationModel != null) {
				Position oldPos = projectionAnnotationModel.getPosition(foldingAnnotation);
				// only update the position if we have to
				if (!newPos.equals(oldPos)) {
					oldPos.setOffset(newPos.offset);
					oldPos.setLength(newPos.length);
					modifications.add(foldingAnnotation);
				}
			} else {
				deletions.add(foldingAnnotation);
			}
		}
	}

	/**
	 * <p>
	 * Searches the given {@link DirtyRegion} for annotations that now have a length
	 * of 0. This is caused when something that was being folded has been deleted.
	 * These {@link FoldingAnnotation}s are then added to the {@link List} of
	 * {@link FoldingAnnotation}s to be deleted
	 * </p>
	 * 
	 * @param deletions the current list of {@link FoldingAnnotation}s marked for
	 *                  deletion that the newly found invalid
	 *                  {@link FoldingAnnotation}s will be added to
	 */
	protected void markInvalidAnnotationsForDeletion(List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing) {
		Iterator<Annotation> iter = projectionAnnotationModel.getAnnotationIterator();
		if (iter != null) {
			while (iter.hasNext()) {
				Annotation anno = iter.next();
				if (anno instanceof FoldingAnnotation) {
					FoldingAnnotation folding = (FoldingAnnotation) anno;
					Position pos = projectionAnnotationModel.getPosition(anno);
					if (pos.length == 0) {
						deletions.add(folding);
					} else {
						existing.add(folding);
					}
				}
			}
		}
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion partition) {
		// Do nothing
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	public void initialReconcile() {
		reconcile(null);
	}
}
