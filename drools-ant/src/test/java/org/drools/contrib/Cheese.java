package org.drools.contrib;

import java.io.Serializable;

public class Cheese
    implements
    Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 400L;
    private String            type;
    private int               price;

    public Cheese() {

    }

    public Cheese(final String type,
                  final int price) {
        super();
        this.type = type;
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public String toString() {
        return "Cheese( type='" + this.type + "', price=" + this.price + " )";
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + price;
        result = PRIME * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Cheese other = (Cheese) obj;
        if ( price != other.price ) return false;
        if ( type == null ) {
            if ( other.type != null ) return false;
        } else if ( !type.equals( other.type ) ) return false;
        return true;
    }

}