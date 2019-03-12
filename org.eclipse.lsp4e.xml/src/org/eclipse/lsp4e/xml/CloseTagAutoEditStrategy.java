package org.eclipse.lsp4e.xml;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.TextDocumentPositionParams;

public class CloseTagAutoEditStrategy implements IAutoEditStrategy {

	private List<LSPDocumentInfo> docInfos;

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (docInfos == null) {
			CompletableFuture.supplyAsync(() -> {
				docInfos = LanguageServiceAccessor.getLSPDocumentInfosFor(document, (capabilities) -> true);
				return docInfos;
			});
			return;
		}
		for (LSPDocumentInfo info : docInfos) {
			if (info.getLanguageClient() instanceof XMLLanguageServerInterface) {
				try {
					TextDocumentPositionParams params = LSPEclipseUtils.toTextDocumentPosistionParams(info.getFileUri(),
							command.offset, document);
					String closeTag = ((XMLLanguageServerInterface) info.getLanguageClient()).closeTag(params).get();
					if (closeTag != null) {
						command.text = closeTag;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
