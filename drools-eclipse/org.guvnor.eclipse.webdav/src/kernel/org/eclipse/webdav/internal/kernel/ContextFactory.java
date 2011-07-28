package org.eclipse.webdav.internal.kernel;

import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IContextFactory;

public class ContextFactory implements IContextFactory {

    protected IContext defaults = new Context();

    public ContextFactory() {
        super();
    }

    public IContext newContext() {
        return new Context(defaults);
    }

    public IContext newContext(IContext baseContext) {
        return new Context(baseContext);
    }

    /**
     * Set the default values on the factory. All contexts created by this
     * factory will have these defaults.
     *
     * @param defaultValues the default values
     */
    public void setDefaults(IContext defaultValues) {
        defaults = defaultValues;
    }
}
