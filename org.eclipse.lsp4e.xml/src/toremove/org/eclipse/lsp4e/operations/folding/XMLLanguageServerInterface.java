/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package toremove.org.eclipse.lsp4e.operations.folding;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

import toremove.org.eclipse.lsp4j.FoldingRange;
import toremove.org.eclipse.lsp4j.FoldingRangeRequestParams;

public interface XMLLanguageServerInterface extends LanguageServer/* , DocumentColorProvider */ {

	@JsonRequest("textDocument/foldingRanges")
	CompletableFuture<List<FoldingRange>> foldingRanges(FoldingRangeRequestParams params);

}
