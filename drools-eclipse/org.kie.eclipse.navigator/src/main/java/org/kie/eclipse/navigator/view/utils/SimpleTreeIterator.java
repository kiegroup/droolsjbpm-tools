package org.kie.eclipse.navigator.view.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SimpleTreeIterator<T extends Object>  implements Iterable<T> {

	public SimpleTreeIterator() {
	}
	
	public abstract Iterator<T> iterator();
	
	@SuppressWarnings("unchecked")
	public class TreeIterator implements Iterator<T> {
		int iteratorIndex = -1;
		int childIndex = 0;
		List<T> children;
		List<Iterator<T>> iterators;
		
		public TreeIterator(List<Object> children) {
			this.children = (List<T>)children;
		}
		
		public boolean hasChildren() {
			return children.size()>0;
		}
		
		@Override
		public boolean hasNext() {
			if (!hasChildren())
				return false;
			
			if (iteratorIndex==-1) {
				iteratorIndex = 0;
				iterators = new ArrayList<Iterator<T>>();
				for (T child : children) {
					iterators.add(((Iterable<T>)child).iterator());
				}
			}
			return iteratorIndex<iterators.size() || childIndex<children.size();
		}

		@Override
		public T next() {
			if (!hasChildren())
				return null;
			while (iteratorIndex<iterators.size()) {
				Iterator<T> iter = iterators.get(iteratorIndex);
				if (iter.hasNext())
					return iter.next();
				++iteratorIndex;
			}
			if (childIndex<children.size()) {
				T child = children.get(childIndex);
				++childIndex;
				return child;
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
