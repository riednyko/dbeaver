/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.ui.controls.lightgrid.renderers;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.ui.controls.lightgrid.LightGrid;

/**
 * The row header renderer.
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class DefaultRowHeaderRenderer extends AbstractRenderer {
    private static final int leftMargin = 6;
    private static final int rightMargin = 8;

    public DefaultRowHeaderRenderer(LightGrid grid) {
        super(grid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(GC gc) {
        String text = getHeaderText();

        gc.setFont(getDisplay().getSystemFont());

        Color background = getHeaderBackground();
        if (background == null) {
            background = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        gc.setBackground(background);

        if (isSelected()) {
            gc.setBackground(grid.getCellHeaderSelectionBackground());
        }

        gc.fillRectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height + 1);


        {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

            gc.drawLine(
                getBounds().x + getBounds().width - 1,
                getBounds().y,
                getBounds().x + getBounds().width - 1,
                getBounds().y + getBounds().height - 1);
            gc.drawLine(
                getBounds().x,
                getBounds().y + getBounds().height - 1,
                getBounds().x + getBounds().width - 1,
                getBounds().y + getBounds().height - 1);
        }

        int x = leftMargin;

        Image image = getHeaderImage();

        if (image != null) {
            gc.drawImage(image, x, getBounds().y + (getBounds().height - image.getBounds().height) / 2);
            x += image.getBounds().width + 5;
        }

        int width = getBounds().width - x;

        width -= rightMargin;

        Color foreground = getHeaderForeground();
        if (foreground == null) {
            foreground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        }

        gc.setForeground(foreground);


        int y = getBounds().y;
        int selectionOffset = 0;

        y += (getBounds().height - gc.stringExtent(text).y) / 2;
        gc.drawString(org.jkiss.dbeaver.ui.TextUtils.getShortString(gc, text, width), getBounds().x + x + selectionOffset, y + selectionOffset, true);
    }

    private Image getHeaderImage() {
        return grid.getRowLabelProvider() == null ? null : grid.getRowLabelProvider().getImage(getRow());
    }

    private String getHeaderText() {
        String text = grid.getRowLabelProvider() == null ? null : grid.getRowLabelProvider().getText(getRow());
        if (text == null) {
            text = String.valueOf(getRow());
        }
        return text;
    }

    private Color getHeaderBackground() {
        if (grid.getRowLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) grid.getRowLabelProvider()).getBackground(getRow());
        } else {
            return null;
        }
    }

    private Color getHeaderForeground() {
        if (grid.getRowLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) grid.getRowLabelProvider()).getForeground(getRow());
        } else {
            return null;
        }
    }

}
