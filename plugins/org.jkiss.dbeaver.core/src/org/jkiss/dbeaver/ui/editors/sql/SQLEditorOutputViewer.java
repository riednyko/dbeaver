/*
 * Copyright (C) 2010-2014 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.editors.sql;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.jkiss.dbeaver.ui.UIUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * SQL content type describer
 */
public class SQLEditorOutputViewer extends Composite {

    private final StyledText text;
    private PrintWriter writer;
    private boolean hasNewOutput;

    public SQLEditorOutputViewer(final IWorkbenchPartSite site, Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        text = new StyledText(this, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);

        text.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e)
            {
                UIUtils.enableHostEditorKeyBindings(site, false);
            }
            @Override
            public void focusLost(FocusEvent e)
            {
                UIUtils.enableHostEditorKeyBindings(site, true);
            }
        });
        Writer out = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                text.append(String.valueOf(cbuf, off, len));
                if (len > 0) {
                    hasNewOutput = true;
                }
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        };
        writer = new PrintWriter(out, true);
        refreshStyles();
    }

    void refreshStyles() {
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        Font outputFont = currentTheme.getFontRegistry().get(SQLConstants.CONFIG_FONT_OUTPUT);
        if (outputFont != null) {
            this.text.setFont(outputFont);
        }
        this.text.setForeground(currentTheme.getColorRegistry().get(SQLConstants.CONFIG_COLOR_TEXT));
        this.text.setBackground(currentTheme.getColorRegistry().get(SQLConstants.CONFIG_COLOR_BACKGROUND));
    }

    void print(String out)
    {
        text.append(out);
    }

    void println(String out)
    {
        print(out + "\n");
    }

    void scrollToEnd()
    {
        text.setTopIndex(text.getLineCount() - 1);
    }

    public PrintWriter getOutputWriter() {
        return writer;
    }

    public boolean isHasNewOutput() {
        return hasNewOutput;
    }

    void resetNewOutput() {
        hasNewOutput = false;
    }
}
