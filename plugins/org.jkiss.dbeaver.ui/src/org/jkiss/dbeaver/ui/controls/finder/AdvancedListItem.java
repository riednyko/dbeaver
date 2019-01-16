/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.controls.finder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Label;
import org.jkiss.dbeaver.Log;

/**
 * AdvancedListItem
 */
public class AdvancedListItem extends Canvas {

    private static final Log log = Log.getLog(AdvancedListItem.class);

    private final AdvancedList list;
    private String text;
    private final Image icon;
    private boolean hover;

    public AdvancedListItem(AdvancedList list, String text, Image icon) {
        super(list.getContainer(), SWT.NONE);

        this.list = list;
        this.setBackground(list.getBackground());
        this.text = text;
        this.icon = icon;

/*
        GridLayout gl = new GridLayout(1, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        setLayout(gl);

        Point itemSize = list.getImageSize();
        Label iconLabel = new Label(this, SWT.NONE);
        iconLabel.setBackground(list.getBackground());
        iconLabel.setSize(itemSize);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER);
        gd.widthHint = itemSize.x;
        gd.heightHint = itemSize.y;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        iconLabel.setLayoutData(gd);
        iconLabel.addPaintListener(e -> {
            Point size = iconLabel.getSize();
            list.paintIcon(e.gc, e.x, e.y, size.x, size.y, this);
        });

        Label textLabel = new Label(this, SWT.CENTER);
        textLabel.setText(text);
        textLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
*/

        this.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                hover = true;
                redraw();
            }

            @Override
            public void mouseExit(MouseEvent e) {
                hover = false;
                redraw();
            }

            @Override
            public void mouseHover(MouseEvent e) {
                //redraw();
            }
        });
        this.addPaintListener(this::painItem);
    }

    private void painItem(PaintEvent e) {
        Point itemSize = getSize();

        GC gc = e.gc;
        if (hover) {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
            gc.fillRoundRectangle(5, 5, itemSize.x - 10, itemSize.y - 10, 5, 5);
        } else {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        }

        Rectangle iconBounds = icon.getBounds();
        Point imageSize = getList().getImageSize();

        int imgPosX = (itemSize.x - imageSize.x) / 2;
        int imgPosY = 5;//(itemBounds.height - iconBounds.height) / 2 ;

        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(icon, 0, 0, iconBounds.width, iconBounds.height,
            imgPosX - e.x, imgPosY, imageSize.x, imageSize.y);

        Point textSize = gc.stringExtent(text);
        if (textSize.x > itemSize.x) textSize.x = itemSize.x;

        gc.drawText(text, (itemSize.x - textSize.x) / 2 - e.x, itemSize.y - 25);
    }

    private AdvancedList getList() {
        return list;
    }

    public Image getIcon() {
        return icon;
    }

    public boolean isHover() {
        return hover;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point imageSize = getList().getImageSize();
        return new Point(imageSize.x + 40, imageSize.y + 40);
        //return super.computeSize(wHint, hHint, changed);//getList().getImageSize();
    }

    private Image resize(Image image, int width, int height) {
        Image scaled = new Image(getDisplay().getDefault(), width, height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0,  0,
            image.getBounds().width, image.getBounds().height,
            0, 0, width, height);
        gc.dispose();
        return scaled;
    }

}