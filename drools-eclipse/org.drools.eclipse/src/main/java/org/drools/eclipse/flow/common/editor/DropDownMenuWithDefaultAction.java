package org.drools.eclipse.flow.common.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class DropDownMenuWithDefaultAction extends Action
    implements
    IMenuCreator {

    private Menu                    dropDownMenu;

    private IAction                 delegate;

    private List                    list;

    private IPropertyChangeListener enabledListener;
    private SelectionListener       selectionListener;

    public DropDownMenuWithDefaultAction(final IAction action) {
        this.selectionListener = new ItemSelectionListener( this );
        setMenuCreator( this );
        this.dropDownMenu = null;
        setAction( action );
        this.list = new ArrayList();
    }

    public void dispose() {
        if ( this.dropDownMenu != null ) {
            this.dropDownMenu.dispose();
            this.dropDownMenu = null;
        }
    }

    public void add(final IContributionItem item) {
        this.list.add( item );
    }

    public void add(final IAction action) {
        this.list.add( action );
    }

    public Menu getMenu(final Control parent) {
        if ( this.dropDownMenu == null ) {
            this.dropDownMenu = new Menu( parent );
            populateMenu();
        }
        return this.dropDownMenu;
    }

    public Menu getMenu(final Menu parent) {
        if ( this.dropDownMenu == null ) {
            this.dropDownMenu = new Menu( parent );
            populateMenu();
        }
        return this.dropDownMenu;
    }

    private void populateMenu() {
        for ( final Iterator it = this.list.iterator(); it.hasNext(); ) {
            final Object object = it.next();
            if ( object instanceof IContributionItem ) {
                final IContributionItem item = (IContributionItem) object;
                item.fill( this.dropDownMenu,
                           -1 );
            } else {
                final IAction action = (IAction) object;
                final ActionContributionItem item = new ActionContributionItem( action );
                item.fill( this.dropDownMenu,
                           -1 );
            }
        }
        final MenuItem[] items = this.dropDownMenu.getItems();
        for ( int i = 0; i < items.length; i++ ) {
            items[i].addSelectionListener( this.selectionListener );
        }
    }

    public void setAction(final IAction action) {
        if ( this.enabledListener == null ) {
            this.enabledListener = new EnabledPropertyChangeListener( this );
        }
        setText( action.getText() );
        setToolTipText( action.getToolTipText() );
        setImageDescriptor( action.getImageDescriptor() );
        setDisabledImageDescriptor( action.getDisabledImageDescriptor() );
        setEnabled( action.isEnabled() );
        setDescription( action.getDescription() );
        setHelpListener( action.getHelpListener() );
        setHoverImageDescriptor( action.getHoverImageDescriptor() );
        if ( this.delegate != null ) {
            this.delegate.removePropertyChangeListener( this.enabledListener );
        }
        this.delegate = action;
        this.delegate.addPropertyChangeListener( this.enabledListener );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run() {
        this.delegate.run();
    }

    public static class EnabledPropertyChangeListener
        implements
        IPropertyChangeListener {
        private IAction action;

        public EnabledPropertyChangeListener(final IAction action) {
            this.action = action;
        }

        public void propertyChange(final PropertyChangeEvent event) {
            if ( event.getProperty().equals( IAction.ENABLED ) ) {
                this.action.setEnabled( ((Boolean) event.getNewValue()).booleanValue() );
            }
        }
    }

    public static class ItemSelectionListener
        implements
        SelectionListener {
        private DropDownMenuWithDefaultAction dropDownMenu;

        public ItemSelectionListener(final DropDownMenuWithDefaultAction dropDownMenu) {
            this.dropDownMenu = dropDownMenu;
        }

        public void widgetDefaultSelected(final SelectionEvent e) {
            final MenuItem menuItem = (MenuItem) e.getSource();
            if ( menuItem.getData() instanceof ActionContributionItem ) {
                final ActionContributionItem item = (ActionContributionItem) menuItem.getData();
                this.dropDownMenu.setAction( item.getAction() );
            }
        }

        public void widgetSelected(final SelectionEvent e) {
            final MenuItem menuItem = (MenuItem) e.getSource();
            if ( menuItem.getData() instanceof ActionContributionItem ) {
                final ActionContributionItem item = (ActionContributionItem) menuItem.getData();
                this.dropDownMenu.setAction( item.getAction() );
            }
        }
    }
}
