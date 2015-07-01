/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.editors.text.handlers;

import org.jkiss.dbeaver.Log;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jkiss.dbeaver.ui.editors.text.BaseTextEditor;

/**
 * This class contains all of the shared functionality for comment handlers
 */
public abstract class AbstractCommentHandler extends AbstractTextHandler {

    static protected final Log log = Log.getLog(AbstractCommentHandler.class);

    public final Object execute(ExecutionEvent event) throws ExecutionException {
        BaseTextEditor textEditor = getEditor(event);
        if (textEditor != null) {
            IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            if (document != null) {
                // get current text selection
                ITextSelection textSelection = getSelection(textEditor);
                if (!textSelection.isEmpty()) {
                    try {
                        processAction(textEditor, document, textSelection);
                    } catch (BadLocationException e) {
                        log.warn(e);
                    }
                }
            }
        }
        return null;
    }

    protected abstract void processAction(BaseTextEditor textEditor, IDocument document, ITextSelection textSelection) throws BadLocationException;

    private static ITextSelection getSelection(ITextEditor textEditor) {
        ISelectionProvider provider = textEditor.getSelectionProvider();
        if (provider != null) {
            ISelection selection = provider.getSelection();
            if (selection instanceof ITextSelection) {
                return (ITextSelection) selection;
            }
        }
        return TextSelection.emptySelection();
    }
}
