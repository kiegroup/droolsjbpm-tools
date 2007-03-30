package org.drools.ide.editors.completion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class DSLTree {

    public static final String when = "[when]";
    public static final String then = "[then]";
    public static final String wildcard = "[*]";
    public static final String separator = "=";
    public static final String tab = "  ";
    
    private Node current = null;
    private Node last = null;
    private Node root = null;
    private boolean empty = true;
    private ArrayList suggestions = new ArrayList();
    private HashMap objToNL = new HashMap();
    
    public DSLTree() {
        this.root = new Node("root");
    }
    
    /**
     * the method will take the dsl file and build a DSLTree using
     * the Node class.
     * @param dslFile
     */
    public void buildTree(String dslFile) {
        // first we clear the children
        this.root.clearChildren();
        BufferedReader breader = openDSLFile(dslFile);
        parseFile(breader);
        try {
            breader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.empty = false;
    }

    /**
     * the method uses the DSLAdapter to get the contents of the
     * DSL mapping file.
     * @param dslcontents
     */
    public void buildTree(Reader dslcontents) {
        this.root.clearChildren();
        BufferedReader breader = this.createBufferedReader(dslcontents);
        parseFile(breader);
        try {
            breader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.empty = false;
    }
    
    /**
     * method will create a BufferedReader to read the file.
     * @param filename
     * @return
     */
    protected BufferedReader openDSLFile(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader breader = new BufferedReader(reader);
            return breader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a buffered reader for the reader created by the DSLAdapater
     * @param reader
     * @return
     */
    protected BufferedReader createBufferedReader(Reader reader) {
		return new BufferedReader(reader);
    }
    
    /**
     * if the DSL mapping hasn't been loaded, the method will return
     * true. If the DSL mapping has been loaded, the method returns
     * false.
     * @return
     */
    public boolean isEmpty() {
    	return this.empty;
    }
    
    /**
     * method will use the BufferedReader to read the contents of the file.
     * It calls other methods to parse the line and build the tree.
     * @param reader
     */
    protected void parseFile(BufferedReader reader) {
        String line = null;
        try {
            while ( (line = reader.readLine()) != null) {
                String nl = stripHeadingAndCode(line);
                String objname = this.getObjMetadata(nl);
                nl = this.stripObjMetadata(nl);
                if (!nl.startsWith("-")) {
                    this.addObjToNLMap(objname, nl);
                    StringTokenizer tokenz = new StringTokenizer(nl);
                    addTokens(tokenz);
                } else {
                	String res = (String)this.objToNL.get(objname);
                    StringTokenizer tokenz = new StringTokenizer(nl);
                    addTokens(res,tokenz);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addObjToNLMap(String objname, String nl) {
    	if (!objname.startsWith("-")) {
    		this.objToNL.put(objname, nl);
    	}
    }
    
    /**
     * method will strip out the when, then, * at the beginning of each 
     * line and the mapped drl expression
     * @param text
     * @return
     */
    protected String stripHeadingAndCode(String text) {
        if (text.startsWith(when)) {
            return text.substring(6,text.indexOf("="));
        } else if (text.startsWith(then)) {
            return text.substring(6,text.indexOf("="));
        } else if (text.startsWith(wildcard)) {
            return text.substring(3,text.indexOf("="));
        } else if (text.startsWith("#")) {
            return "";
        } else {
            return text;
        }
    }
    
    /**
     * Method will return just the object metadata
     * @param text
     * @return
     */
    protected String getObjMetadata(String text) {
    	if (text.startsWith("[")) {
        	return text.substring(1,text.lastIndexOf("]"));
    	} else {
    		return "";
    	}
    }

    /**
     * method will strip the metadata from the text string
     * @param text
     * @return
     */
    protected String stripObjMetadata(String text) {
    	if (text.startsWith("[")) {
        	return text.substring(text.lastIndexOf("]") + 1);
    	} else {
    		return text;
    	}
    }
    
    /**
     * The method is different than addTokens(StringTokenizer). this method
     * expects additional metadata. It expects to get an object name or "*"
     * meaning all. If the metadata is a wildcard all, it will add the
     * tokens to all the top level nodes that are immediate child of root.
     * @param metadata
     * @param tokens
     */
    public void addTokens(String metadata, StringTokenizer tokens) {
    	Node mnode = this.root.addToken(metadata);
    	Node thenode = mnode;
    	while (tokens.hasMoreTokens()) {
    		Node newnode = thenode.addToken(tokens.nextToken());
    		thenode = newnode;
    	}
    }
    
    /**
     * method adds the token to root
     * @param tokens
     */
    public void addTokens(StringTokenizer tokens) {
        Node thenode = this.root;
        while (tokens.hasMoreTokens()) {
            Node newnode = thenode.addToken(tokens.nextToken());
            thenode = newnode;
        }
    }
    
    /**
     * the method will tokenize the text and try to find
     * the node that matches and return the children. the method
     * will traverse down the network as far as it can and return
     * the children at that level.
     * @param text
     * @return
     */
    public Node[] getChildren(String text) {
        Node thenode = this.root;
    	if (text.length() > 0) {
            StringTokenizer tokenz = new StringTokenizer(text);
            this.last = this.current;
            while (tokenz.hasMoreTokens()) {
                String strtk = tokenz.nextToken();
                Node ch = thenode.getChild(strtk);
                // if a child is found, we set thenode to the child Node
                if (ch != null) {
                    thenode = ch;
                } else {
                    break;
                }
            }
            if (thenode != this.root) {
                this.current = thenode;
            }
    	}
        Collection children = thenode.getChildren();
        Node[] nchild = new Node[children.size()];
        return (Node[])children.toArray(nchild);
    }

    /**
     * the method expects the caller to pass the object
     * @param obj
     * @param text
     * @return
     */
    public Node[] getChildren(String obj, String text) {
        Node thenode = this.root.getChild(obj);
    	if (thenode != null && text.length() > 0) {
            StringTokenizer tokenz = new StringTokenizer(text);
            this.last = this.current;
            while (tokenz.hasMoreTokens()) {
                String strtk = tokenz.nextToken();
                Node ch = thenode.getChild(strtk);
                // if a child is found, we set thenode to the child Node
                if (ch != null) {
                    thenode = ch;
                } else {
                    break;
                }
            }
            if (thenode != this.root) {
                this.current = thenode;
            }
    	}
    	if (thenode == null) {
    		thenode = this.root;
    	}
        Collection children = thenode.getChildren();
        Node[] nchild = new Node[children.size()];
        return (Node[])children.toArray(nchild);
    }
    
    /**
     * for convienance, the method will return a list of strings
     * that are children of the last node found. If the editor
     * wants to generate the children strings, call the method
     * with true
     * @param text
     * @return
     */
    public ArrayList getChildrenList(String text, boolean addChildren) {
    	Node[] c = getChildren(text);
    	this.suggestions.clear();
    	for (int idx=0; idx < c.length; idx++) {
    		this.suggestions.add(c[idx].getToken());
    		if (addChildren) {
        		this.addChildToList(c[idx], c[idx].getToken(), this.suggestions);
    		}
    	}
    	return this.suggestions;
    }
    
    /**
     * 
     * @param obj
     * @param text
     * @param addChildren
     * @return
     */
    public ArrayList getChildrenList(String obj, String text, boolean addChildren) {
    	Node[] c = getChildren(obj,text);
    	this.suggestions.clear();
    	for (int idx=0; idx < c.length; idx++) {
    		this.suggestions.add(c[idx].getToken());
    		if (addChildren) {
        		this.addChildToList(c[idx], c[idx].getToken(), this.suggestions);
    		}
    	}
    	// in the event the line is zero length after it is trimmed, we also add
    	// the top level nodes
    	if (text.trim().length() == 0) {
    		Iterator top = this.root.getChildren().iterator();
        	while (top.hasNext()) {
        		Node t = (Node)top.next();
        		if (!this.suggestions.contains(t.getToken())) {
            		this.suggestions.add(t.getToken());
            		if (addChildren) {
                		this.addChildToList(t, t.getToken(), this.suggestions);
            		}
        		}
        	}
    	}
    	return this.suggestions;
    }

    /**
     * method will prepend the parent text to the child and generate
     * the possible combinations in text format.
     * @param n
     * @param prefix
     * @param list
     */
    public void addChildToList(Node n, String prefix, ArrayList list) {
    	if (n.getChildren().size() > 0) {
    		Iterator itr = n.getChildren().iterator();
    		while (itr.hasNext()) {
    			Node child = (Node)itr.next();
    			String text = prefix + " " + child.getToken();
    			list.add(text);
    			addChildToList(child,text,list);
    		}
    	}
    }
    
    public Node getCurrent() {
        return current;
    }

    public void setCurrent(Node current) {
        this.current = current;
    }

    public Node getLast() {
        return last;
    }

    public void setLast(Node last) {
        this.last = last;
    }

    /**
     * The method will print the DSLTree to System.out in text format.
     */
    public void printTree() {
        System.out.println("ROOT");
        Iterator itr = this.root.getChildren().iterator();
        while (itr.hasNext()) {
            Node n = (Node)itr.next();
            printNode(n);
        }
    }
    
    /**
     * method will print the node and then iterate over the children
     * @param n
     */
    protected void printNode(Node n) {
        printTabs(n.getDepth());
        System.out.println("- \"" + n.getToken() + "\"");
        Iterator itr = n.getChildren().iterator();
        while (itr.hasNext()) {
            Node c = (Node)itr.next();
            printNode(c);
        }
    }
    
    /**
     * Method will print n number of tabs
     * @param count
     */
    protected void printTabs(int count) {
        for (int idx=0; idx < count; idx++) {
            System.out.print(tab);
        }
    }
}
