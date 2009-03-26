package org.drools.eclipse.rulebuilder.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.DSLSentence;
import org.drools.guvnor.client.modeldriven.ui.ConstraintValueEditorHelper;
import org.drools.util.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This displays a widget to edit a DSL sentence.
 * 
 * @author Ahti Kitsik
 * @author Anton Arhipov
 */
public abstract class DSLSentenceWidget extends Widget {

    private static final String          ENUM_TAG    = "ENUM";
    private static final String          DATE_TAG    = "DATE";
    private static final String          BOOLEAN_TAG = "BOOLEAN";

    private static final String          ITEM_       = "ITEM_";

    private final DSLSentence            sentence;

    protected SuggestionCompletionEngine completions;

    private List<ModelWidget>            widgets     = new ArrayList<ModelWidget>();

    public DSLSentenceWidget(FormToolkit toolkit,
                             Composite parent,
                             DSLSentence sentence,
                             RuleModeller modeller,
                             int index) {
        super( parent,
               toolkit,
               modeller,
               index );

        this.sentence = sentence;
        completions = modeller.getSuggestionCompletionEngine();

        Composite lastRow = makeWidgets( this.sentence.sentence );
        addDeleteAction( lastRow );
        toolkit.paintBordersFor( parent );
    }

    protected abstract void updateModel();

    private void addDeleteAction(Composite parent) {

        ImageHyperlink delLink = addImage( parent,
                                           "icons/delete_item_small.gif" );
        delLink.addHyperlinkListener( new IHyperlinkListener() {

            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this DSL sentence?" );
                dialog.setText( "Remove this DSL sentence?" );
                if ( dialog.open() == SWT.YES ) {
                    updateModel();
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delLink.setToolTipText( "Remove this condition." );
    }

    /**
     * This will take a DSL line item, and split it into widget thingamies for
     * displaying. One day, if this is too complex, this will have to be done on
     * the server side.
     * @return 
     */
    public Composite makeWidgets(String dslLine) {

        List<ModelWidget> lineWidgets = new ArrayList<ModelWidget>();

        int startVariable = dslLine.indexOf( "{" );

        boolean firstOneIsBracket = (dslLine.indexOf( "{" ) == 0);

        String startLabel = "";
        if ( startVariable > 0 ) {
            startLabel = dslLine.substring( 0,
                                            startVariable );
        } else if ( !firstOneIsBracket ) {
            // There are no curly brackets in the text.
            // Just print it
            startLabel = dslLine;
        }

        parent.setLayout( new GridLayout( 1,
                                          true ) );

        Composite row = toolkit.createComposite( parent );
        RowLayout rl = new RowLayout();
        rl.marginBottom = 0;
        rl.marginHeight = 0;
        rl.marginLeft = 0;
        rl.marginRight = 0;
        rl.pack = true;
        rl.spacing = 0;
        rl.center = true;
        //rl.fill=true;

        row.setLayout( rl );

        lineWidgets.add( new LabelWidget( row,
                                          startLabel ) );

        while ( startVariable > 0 || firstOneIsBracket ) {
            firstOneIsBracket = false;

            int endVariable = dslLine.indexOf( "}",
                                               startVariable );
            String currVariable = dslLine.substring( startVariable + 1,
                                                     endVariable );

            lineWidgets.add( addVariable( row,
                                          currVariable ) );

            // Parse out the next label between variables
            startVariable = dslLine.indexOf( "{",
                                             endVariable );
            String lbl;
            if ( startVariable > 0 ) {
                lbl = dslLine.substring( endVariable + 1,
                                         startVariable );
            } else {
                lbl = dslLine.substring( endVariable + 1,
                                         dslLine.length() );
            }

            if ( lbl.indexOf( "\\n" ) > -1 ) {
                String[] lines = lbl.split( "\\\\n" );
                for ( int i = 0; i < lines.length; i++ ) {
                    row = toolkit.createComposite( parent );
                    row.setLayout( rl );
                    lineWidgets.add( new DSLSentenceWidget.NewLine( row ) );
                    lineWidgets.add( new LabelWidget( row,
                                                      lines[i] ) );
                }
            } else {
                lineWidgets.add( new LabelWidget( row,
                                                  lbl ) );
            }

        }

        for ( ModelWidget widg : lineWidgets ) {
            widgets.add( widg );
        }

        updateSentence();
        return row;
    }

    public ModelWidget addVariable(Composite parent,
                                   String currVariable) {

        // Formats are: <varName>:ENUM:<Field.type>
        // <varName>:DATE:<dateFormat>
        // <varName>:BOOLEAN:[checked | unchecked] <-initial value

        int colonIndex = currVariable.indexOf( ":" );
        if ( colonIndex > 0 ) {

            String definition = currVariable.substring( colonIndex + 1,
                                                        currVariable.length() );

            int secondColonIndex = definition.indexOf( ":" );
            if ( secondColonIndex > 0 ) {

                String type = currVariable.substring( colonIndex + 1,
                                                      colonIndex + secondColonIndex + 1 );
                if ( type.equalsIgnoreCase( ENUM_TAG ) ) {
                    return addEnumDropdown( parent,
                                            currVariable );
                } else if ( type.equalsIgnoreCase( DATE_TAG ) ) {
                    return addDateSelector( parent,
                                            currVariable );
                } else if ( type.equalsIgnoreCase( BOOLEAN_TAG ) ) {
                    return addCheckbox( parent,
                                        currVariable );
                }
            } else {
                String regex = currVariable.substring( colonIndex + 1,
                                                       currVariable.length() );
                return addBox( parent,
                               currVariable,
                               regex );
            }
        }

        return addBox( parent,
                       currVariable,
                       "" );

    }

    public ModelWidget addBox(Composite parent,
                              String variableDef,
                              String regex) {

        int colonIndex = variableDef.indexOf( ":" );
        if ( colonIndex > 0 ) {
            variableDef = variableDef.substring( 0,
                                                 colonIndex );
        }
        FieldEditor currentBox = new FieldEditor( parent );
        currentBox.setVisibleLength( variableDef.length() + 1 );
        currentBox.setText( variableDef );
        currentBox.setRestriction( regex );
        return currentBox;

    }

    public ModelWidget addCheckbox(Composite parent,
                                   String variableDef) {
        return new DSLCheckBox( parent,
                                variableDef );
    }

    public ModelWidget addDateSelector(Composite parent,
                                       String variableDef) {
        return new DSLDateSelector( parent,
                                    variableDef );
    }

    private ModelWidget addEnumDropdown(Composite parent,
                                        String variableDef) {
        return new DSLDropDown( parent,
                                variableDef );
    }

    protected void updateSentence() {
        String newSentence = "";
        for ( Iterator iter = widgets.iterator(); iter.hasNext(); ) {
            ModelWidget wid = (ModelWidget) iter.next();
            if ( wid instanceof LabelWidget ) {
                newSentence = newSentence + ((LabelWidget) wid).getText();
            } else if ( wid instanceof FieldEditor ) {
                FieldEditor editor = (FieldEditor) wid;

                String varString = editor.getText();
                String restriction = editor.getRestriction();
                if ( !restriction.equals( "" ) ) {
                    varString = varString + ":" + restriction;
                }

                newSentence = newSentence + " {" + varString + "} ";
            } else if ( wid instanceof DSLDropDown ) {

                // Add the meta-data back to the field so that is shows up as a
                // dropdown when refreshed from repo
                DSLDropDown drop = (DSLDropDown) wid;
                Combo box = (Combo) drop.getControl();
                String type = drop.getType();
                String factAndField = drop.getFactAndField();

                String key = ITEM_ + (box.getSelectionIndex() + 1);

                Object keyval = box.getData( key );

                newSentence = newSentence + "{" + keyval + ":" + type + ":" + factAndField + "} ";
            } else if ( wid instanceof DSLCheckBox ) {

                DSLCheckBox check = (DSLCheckBox) wid;
                String checkValue = check.getCheckedValue();
                newSentence = newSentence + "{" + checkValue + ":" + check.getType() + ":" + checkValue + "} ";
            } else if ( wid instanceof DSLDateSelector ) {
                DSLDateSelector dateSel = (DSLDateSelector) wid;
                String dateString = dateSel.getDateString();
                String format = dateSel.getJavascriptFormat();
                newSentence = newSentence + "{" + dateString + ":" + dateSel.getType() + ":" + format + "} ";
            } else if ( wid instanceof NewLine ) {
                newSentence = newSentence + "\\n";
            }

        }
        this.sentence.sentence = newSentence.trim();
    }

    class LabelWidget
        implements
        ModelWidget {

        private Label  control;
        private String val;

        public LabelWidget(Composite parent,
                           String text) {
            val = text;
            control = toolkit.createLabel( parent,
                                           text );
        }

        public Control getControl() {
            return control;
        }

        public String getText() {
            return val;
        }

    }

    class NewLine
        implements
        ModelWidget {

        private Composite control;

        public NewLine(Composite parent) {
            control = toolkit.createComposite( parent );
        }

        public Control getControl() {
            return control;
        }
    }

    class FieldEditor
        implements
        ModelWidget {

        private Text   control;

        private String oldValue = "";
        private String regex    = "";

        public FieldEditor(Composite parent) {

            control = toolkit.createText( parent,
                                          "" );
            // box.setStyleName( "dsl-field-TextBox" );

            control.addModifyListener( new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    Text otherBox = control;

                    if ( !regex.equals( "" ) && !otherBox.getText().matches( regex ) ) {
                        System.err.println( "VALUE IS NOT VALID: " + otherBox.getText() );
                        control.setText( oldValue );
                    } else {
                        oldValue = otherBox.getText();
                        updateSentence();
                        getModeller().setDirty( true );
                    }
                }
            } );

        }

        public void setText(String t) {
            control.setText( t );
        }

        public void setVisibleLength(int l) {
            //box.set
            //TODO Implement!
        }

        public Control getControl() {
            return control;
        }

        public String getText() {
            return control.getText();
        }

        public void setRestriction(String regex) {
            this.regex = regex;
        }

        public String getRestriction() {
            return this.regex;
        }

        public boolean isValid() {
            boolean result = true;
            if ( !regex.equals( "" ) ) result = this.control.getText().matches( this.regex );

            return result;
        }
    }

    class DSLDropDown
        implements
        ModelWidget {

        Combo          resultWidget = null;
        // Format for the dropdown def is <varName>:<type>:<Fact.field>
        private String varName      = "";
        private String type         = "";
        private String factAndField = "";

        public DSLDropDown(Composite parent,
                           String variableDef) {
            Composite comp = toolkit.createComposite( parent );

            comp.setLayout( new FillLayout() );

            int firstIndex = variableDef.indexOf( ":" );
            int lastIndex = variableDef.lastIndexOf( ":" );
            varName = variableDef.substring( 0,
                                             firstIndex );
            type = variableDef.substring( firstIndex + 1,
                                          lastIndex );
            factAndField = variableDef.substring( lastIndex + 1,
                                                  variableDef.length() );

            int dotIndex = factAndField.indexOf( "." );
            String type = factAndField.substring( 0,
                                                  dotIndex );
            String field = factAndField.substring( dotIndex + 1,
                                                   factAndField.length() );

            String[] data = completions.getEnumValues( type,
                                                       field );

            Combo list = new Combo( comp,
                                    SWT.DROP_DOWN | SWT.READ_ONLY );

            if ( data != null ) {

                int selected = 0; // Select first item for now by default. Null values are not allowed.

                for ( int i = 0; i < data.length; i++ ) {
                    String realValue = data[i];
                    String display = data[i];
                    if ( data[i].indexOf( '=' ) > -1 ) {
                        String[] vs = ConstraintValueEditorHelper.splitValue( data[i] );
                        realValue = vs[0];
                        display = vs[1];
                    }
                    if ( varName.equals( realValue ) ) {
                        selected = i;
                    }

                    list.add( display );

                    String key = ITEM_ + list.getItemCount();
                    list.setData( key,
                                  realValue );

                }
                if ( selected >= 0 ) list.select( selected );
            }

            list.addModifyListener( new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    updateSentence();
                    getModeller().setDirty( true );
                }

            } );

            resultWidget = list;
            comp.layout();
        }

        public Control getControl() {
            return resultWidget;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFactAndField() {
            return factAndField;
        }

        public void setFactAndField(String factAndField) {
            this.factAndField = factAndField;
        }
    }

    class DSLCheckBox
        implements
        ModelWidget {
        Combo             resultWidget = null;
        // Format for the dropdown def is <varName>:<type>:<Fact.field>
        private String    varName      = "";
        private Composite control;

        public DSLCheckBox(Composite parent,
                           String variableDef) {

            control = toolkit.createComposite( parent );
            control.setLayout( new RowLayout() );

            int firstIndex = variableDef.indexOf( ":" );
            int lastIndex = variableDef.lastIndexOf( ":" );
            varName = variableDef.substring( 0,
                                             firstIndex );
            String checkedUnchecked = variableDef.substring( lastIndex + 1,
                                                             variableDef.length() );

            resultWidget = new Combo( control,
                                      SWT.READ_ONLY );
            resultWidget.add( "true" );
            resultWidget.add( "false" );

            if ( checkedUnchecked.equalsIgnoreCase( "checked" ) ) {
                resultWidget.select( 0 );
            } else {
                resultWidget.select( 1 );
            }

            resultWidget.addModifyListener( new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    updateSentence();
                    modeller.setDirty( true );
                }

            } );

            resultWidget.setVisible( true );
            control.layout();

        }

        public Control getControl() {
            return control;
        }

        public Combo getListBox() {
            return resultWidget;
        }

        public void setListBox(Combo resultWidget) {
            this.resultWidget = resultWidget;
        }

        public String getType() {
            return BOOLEAN_TAG;
        }

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getCheckedValue() {
            return this.resultWidget.getSelectionIndex() == 0 ? "checked" : "checked";

        }
    }

    class DSLDateSelector
        implements
        ModelWidget {

        //DateTime              resultWidget            = null;

        // Format for the dropdown def is <varName>:<type>:<Fact.field>
        private String           varName                 = "";
        private String           javascriptFormat        = "";
        private final String     defaultJavascriptFormat = "d-M-y";
        private final String     javaFormat              = DateUtils.getDateFormatMask();
        private SimpleDateFormat formatter               = null;
        private Composite        control;
        private Text             field;

        public DSLDateSelector(final Composite parent,
                               String variableDef) {

            control = toolkit.createComposite( parent );
            control.setLayout( new RowLayout() );

            int firstIndex = variableDef.indexOf( ":" );
            int lastIndex = variableDef.lastIndexOf( ":" );
            varName = variableDef.substring( 0,
                                             firstIndex );
            javascriptFormat = variableDef.substring( lastIndex + 1,
                                                      variableDef.length() );

            // Resolve the javascript format
            if ( javascriptFormat.equals( "" ) || javascriptFormat.equals( "default" ) ) {
                javascriptFormat = defaultJavascriptFormat;
            }

            // Set the java format for formatter
            formatter = new SimpleDateFormat( javaFormat );

            Date origDate = new Date();
            if ( !varName.equals( "" ) ) {
                try {
                    origDate = formatter.parse( varName );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }

            field = toolkit.createText( control,
                                        "" );
            final Button open = new Button( control,
                                            SWT.ARROW | SWT.DOWN );
            //open.setText ("Set");

            if ( origDate != null ) field.setText( formatter.format( origDate ) );

            field.addModifyListener( new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateSentence();
                    modeller.setDirty( true );
                }
            } );

            open.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    final Shell dialog = new Shell( open.getShell(),
                                                    SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
                    dialog.setText( "Set date:" );
                    dialog.setLayout( new GridLayout( 1,
                                                      false ) );
                    final DateTime calendar = new DateTime( dialog,
                                                            SWT.CALENDAR );

                    Date date = new Date();
                    try {
                        String txt = field.getText();
                        if ( txt != null && txt.length() > 0 ) {
                            date = formatter.parse( txt );
                        }
                    } catch ( ParseException e1 ) {
                        e1.printStackTrace();
                    }

                    calendar.setDate( date.getYear() + 1900,
                                      date.getMonth(),
                                      date.getDate() );

                    Point p2 = open.toDisplay( 0,
                                               0 );

                    int x = p2.x;
                    int y = p2.y + 20;

                    dialog.setLocation( x,
                                        y );

                    Button ok = new Button( dialog,
                                            SWT.PUSH );
                    ok.setText( "OK" );
                    ok.setLayoutData( new GridData( SWT.FILL,
                                                    SWT.CENTER,
                                                    false,
                                                    false ) );
                    ok.addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent e) {
                            field.setText( formatter.format( new Date( calendar.getYear() - 1900,
                                                                       calendar.getMonth(),
                                                                       calendar.getDay() ) ) );
                            updateSentence();
                            modeller.setDirty( true );
                            dialog.close();
                        }
                    } );
                    dialog.setDefaultButton( ok );
                    dialog.pack();
                    dialog.open();
                }
            } );

            control.layout();

        }

        public Control getControl() {
            return control;
        }

        public String getType() {
            return DATE_TAG;
        }

        public String getJavascriptFormat() {
            return this.javascriptFormat;
        }

        public String getDateString() {
            Date value = new Date();
            try {
                String txt = field.getText();
                if ( txt != null && txt.length() > 0 ) {
                    value = formatter.parse( txt );
                }
            } catch ( ParseException e ) {
                e.printStackTrace();
                return "";
            }
            String result = "";
            if ( value != null ) result = formatter.format( value );
            else result = varName;

            return result;
        }

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

    }

}
