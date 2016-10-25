/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.lang.base;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.lang.SCMCompositeNode;
import org.jkiss.dbeaver.lang.SCMGroupNode;
import org.jkiss.dbeaver.lang.SCMLeafNode;
import org.jkiss.dbeaver.lang.SCMSourceScanner;

/**
 * Leaf node - linked to particular source code segment
 */
public class SCMEString extends SCMLeafNode {

    public SCMEString(@NotNull SCMCompositeNode parent, @NotNull SCMSourceScanner scanner) {
        super(parent, scanner);
        if (endOffset - beginOffset < 2) {
            throw new IllegalArgumentException("String node length must be >= 2 (" + (endOffset - beginOffset) + " specified");
        }
    }

    public String getStringValue() {
        return parent.getSource().getSegment(beginOffset + 1, endOffset - 1);
    }

    public char getOpenQuote() {
        return parent.getSource().getChar(beginOffset);
    }

    public char getCloseQuote() {
        return parent.getSource().getChar(endOffset);
    }

}
