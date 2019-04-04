package org.jkiss.dbeaver.ext.erd.editor.tools;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.ext.erd.editor.ERDEditorPart;
import org.jkiss.dbeaver.ext.erd.part.ICustomizablePart;
import org.jkiss.dbeaver.ext.erd.part.NodePart;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class SetPartSettingsAction extends SelectionAction {

    private IStructuredSelection selection;

    public SetPartSettingsAction(ERDEditorPart part, IStructuredSelection selection) {
        super(part);
        this.selection = selection;

        this.setText("View settings");
        this.setToolTipText("Figure view settings");
        this.setId("setPartSettings"); //$NON-NLS-1$
    }

    protected boolean calculateEnabled() {
        for (Object item : selection.toArray()) {
            if (item instanceof NodePart) {
                return true;
            }
        }
        return false;
    }

    protected void init() {
        super.init();
    }

    public void run() {
        this.execute(this.createColorCommand(selection.toArray()));
    }

    private Command createColorCommand(final Object[] objects) {
        return new Command() {
            private final Map<ICustomizablePart, Color> oldColors = new HashMap<>();
            private Color newBackground;
            private Color newForeground;
            private int newBorderWidth;
            private int newTransparency;
            private String newFontName;
            @Override
            public void execute() {
                final Shell shell = UIUtils.createCenteredShell(getWorkbenchPart().getSite().getShell());
                try {
                    ColorDialog colorDialog = new ColorDialog(shell);
                    RGB color = colorDialog.open();
                    if (color == null) {
                        return;
                    }
                    newBackground = new Color(Display.getCurrent(), color);
                    for (Object item : objects) {
                        if (item instanceof ICustomizablePart) {
                            ICustomizablePart colorizedPart = (ICustomizablePart) item;
                            oldColors.put(colorizedPart, colorizedPart.getCustomBackgroundColor());
                            colorizedPart.setCustomBackgroundColor(newBackground);
                        }
                    }
                } finally {
                    shell.dispose();
                }
            }

            @Override
            public void undo() {
                for (Object item : objects) {
                    if (item instanceof ICustomizablePart) {
                        ICustomizablePart colorizedPart = (ICustomizablePart) item;
                        colorizedPart.setCustomBackgroundColor(oldColors.get(colorizedPart));
                    }
                }
            }

            @Override
            public void redo() {
                for (Object item : objects) {
                    if (item instanceof ICustomizablePart) {
                        ICustomizablePart colorizedPart = (ICustomizablePart) item;
                        colorizedPart.setCustomBackgroundColor(newBackground);
                    }
                }
            }
        };
    }


}
