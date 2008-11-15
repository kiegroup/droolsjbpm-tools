package org.drools.eclipse.flow.common.editor.editpart.figure;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

public class FixedConnectionAnchor extends AbstractConnectionAnchor {

	public boolean leftToRight = true;
	public int offsetH;
	public int offsetV;
	public boolean topDown = true;

	public FixedConnectionAnchor(IFigure owner) {
		super(owner);
	}

	public void ancestorMoved(IFigure figure) {
		if (figure instanceof ScalableFigure) {
			return;
		}
		super.ancestorMoved(figure);
	}

	public Point getLocation(Point reference) {
		Rectangle r = getOwner().getBounds();
		int x, y;
		if (topDown) {
			y = r.y + offsetV;
		} else {
			y = r.bottom() - 1 - offsetV;
		}
		if (leftToRight) {
			x = r.x + offsetH;
		} else {
			x = r.right() - 1 - offsetH;
		}
		Point p = new PrecisionPoint(x, y);
		getOwner().translateToAbsolute(p);
		return p;
	}

	public Point getReferencePoint() {
		return getLocation(null);
	}

	public void setOffsetH(int offsetH) {
		this.offsetH = offsetH;
		fireAnchorMoved();
	}

	public void setOffsetV(int offsetV) {
		this.offsetV = offsetV;
		fireAnchorMoved();
	}

	public boolean equals(Object o) {
		if (o instanceof FixedConnectionAnchor) {
			FixedConnectionAnchor fa = (FixedConnectionAnchor) o;
			if (fa.leftToRight == this.leftToRight
					&& fa.topDown == this.topDown && fa.offsetH == this.offsetH
					&& fa.offsetV == this.offsetV
					&& fa.getOwner() == this.getOwner()) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return ((this.leftToRight ? 31 : 0) + (this.topDown ? 37 : 0)
				+ this.offsetH * 43 + this.offsetV * 47)
				^ this.getOwner().hashCode();
	}

}
