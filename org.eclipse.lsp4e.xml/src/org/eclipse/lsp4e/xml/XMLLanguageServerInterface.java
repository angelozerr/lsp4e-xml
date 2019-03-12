package org.eclipse.lsp4e.xml;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

public interface XMLLanguageServerInterface extends LanguageServer  {

	@JsonRequest("xml/closeTag")
	CompletableFuture<String> closeTag(TextDocumentPositionParams params);
}
