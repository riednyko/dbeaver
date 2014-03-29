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
package org.jkiss.dbeaver.ui.controls.lightgrid;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ui.DBIcon;

/**
 * Grid row header renderer.
 */
class GridRowRenderer extends AbstractRenderer {

    static final Image IMG_EXPAND = DBIcon.TREE_EXPAND.getImage();
    static final Image IMG_COLLAPSE = DBIcon.TREE_COLLAPSE.getImage();
    static final Rectangle EXPANDED_BOUNDS = IMG_EXPAND.getBounds();

    public static final int LEFT_MARGIN = 4;
    public static final int RIGHT_MARGIN = 4;
    public static final int IMAGE_SPACING = 5;
    public static final int EXPANDER_SPACING = 2;
    public static final int LEVEL_SPACING = EXPANDED_BOUNDS.width;

    private final Color DEFAULT_BACKGROUND;
    private final Color DEFAULT_FOREGROUND;
    private final Color DEFAULT_FOREGROUND_TEXT;

    private int level;
    private IGridContentProvider.ElementState state;

    public GridRowRenderer(LightGrid grid) {
        super(grid);
        DEFAULT_BACKGROUND = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        DEFAULT_FOREGROUND = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        DEFAULT_FOREGROUND_TEXT = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
    }

    @Override
    public void paint(GC gc) {
        String text = grid.getLabelProvider().getText(cell.row);

        gc.setFont(getDisplay().getSystemFont());

        Color background = getHeaderBackground();
        if (background == null) {
            background = DEFAULT_BACKGROUND;
        }
        gc.setBackground(background);

        if (isSelected()) {
            gc.setBackground(grid.getCellHeaderSelectionBackground());
        }

        gc.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height + 1);


        {
            gc.setForeground(DEFAULT_FOREGROUND);

            gc.drawLine(
                bounds.x + bounds.width - 1,
                bounds.y,
                bounds.x + bounds.width - 1,
                bounds.y + bounds.height - 1);
            gc.drawLine(
                bounds.x,
                bounds.y + bounds.height - 1,
                bounds.x + bounds.width - 1,
                bounds.y + bounds.height - 1);
        }

        int x = LEFT_MARGIN;
        if (level > 0) {
            x += level * LEVEL_SPACING;
        }
        if (state != IGridContentProvider.ElementState.NONE) {
            Image expandImage = state == IGridContentProvider.ElementState.EXPANDED ? IMG_COLLAPSE : IMG_EXPAND;
            gc.drawImage(expandImage, x, bounds.y + (bounds.height - EXPANDED_BOUNDS.height) / 2);
            x += EXPANDED_BOUNDS.width + EXPANDER_SPACING;
        } else if (grid.hasNodes()) {
            x += EXPANDED_BOUNDS.width + EXPANDER_SPACING;
        }

        Image image = grid.getLabelProvider().getImage(cell.row);

        if (image != null) {
            gc.drawImage(image, x, bounds.y + (bounds.height - image.getBounds().height) / 2);
            x += image.getBounds().width + IMAGE_SPACING;
        }

        int width = bounds.width - x;

        width -= RIGHT_MARGIN;

        Color foreground = getHeaderForeground();
        if (foreground == null) {
            foreground = DEFAULT_FOREGROUND_TEXT;
        }

        gc.setForeground(foreground);

        int y = bounds.y;
        int selectionOffset = 0;

        y += (bounds.height - gc.stringExtent(text).y) / 2;
        gc.drawString(org.jkiss.dbeaver.ui.TextUtils.getShortString(grid.fontMetrics, text, width), bounds.x + x + selectionOffset, y + selectionOffset, true);
    }

    @Nullable
    protected Color getHeaderBackground() {
        return grid.getLabelProvider().getBackground(cell.row);
    }

    @Nullable
    protected Color getHeaderForeground() {
        return grid.getLabelProvider().getForeground(cell.row);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    void setState(IGridContentProvider.ElementState state) {
        this.state = state;
    }

    public int computeHeaderWidth(Object element, int level) {
        int width = GridRowRenderer.LEFT_MARGIN + GridRowRenderer.RIGHT_MARGIN;
        if (grid.hasNodes()) {
            width += GridRowRenderer.EXPANDED_BOUNDS.width + EXPANDER_SPACING;
        }
        Image rowImage = grid.getLabelProvider().getImage(element);
        if (rowImage != null) {
            width += rowImage.getBounds().width;
            width += GridRowRenderer.IMAGE_SPACING;
        }
        String rowText = grid.getLabelProvider().getText(element);
        Point ext = grid.sizingGC.stringExtent(rowText);
        width += ext.x;
        width += level * GridRowRenderer.LEVEL_SPACING;
        return width;
    }
}
