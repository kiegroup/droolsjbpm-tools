package org.drools.eclipse.editors.completion;

import java.util.Collection;
import java.util.HashMap;

public class Node {
    private HashMap children = new HashMap();
    private Node parent = null;
    private String token;
    private int depth = 0;
    
    public Node(String name) {
        this.token = name;
    }

    /**
     * The method will create a new Node instance and try to add it as
     * a child node. If an Node with the same string token exists, the
     * method will return the existing node instead.
     * @param token
     * @return
     */
    public Node addToken(String token) {
        Node newnode = new Node(token);
        // set the depth first
        newnode.setDepth(depth + 1);
        // add the node as a child
        newnode = addChild(newnode);
        return newnode;
    }
    
    /**
     * if the string matches this node's token, the method will return
     * true. Otherwise it returns false.
     * @param input
     * @return
     */
    public boolean isMatch(String input) {
        return input.equals(token);
    }

    public boolean isMatch(Node n) {
        return this.token.equals(n.getToken());
    }
    
    /**
     * The method will check to see if a Node with the same string token
     * already exists. If it doesn't, it will add the token as a child and
     * return the same node.
     * 
     * On the otherhand, if there is an existing Node for the same string
     * token, the method returns the existing Node instance.
     * @param n
     * @return
     */
    public Node addChild(Node n) {
        if (!this.children.containsKey(n.getToken())) {
            this.children.put(n.getToken(),n);
            n.setParent(this);
            return n;
        } else {
            return (Node)this.children.get(n.getToken());
        }
    }
    
    public void removeChild(Node n) {
        this.children.remove(n.getToken());
    }
    
    public Collection getChildren() {
        return this.children.values();
    }

    /**
     * The method will get the child matching the string token
     * @param token
     * @return
     */
    public Node getChild(String token) {
        return (Node)this.children.get(token);
    }
    
    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    public void clearChildren() {
        this.children.clear();
    }
}
