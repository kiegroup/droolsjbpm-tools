package org.kie.eclipse.navigator.view.actions.dialogs;

import com.eclipsesource.json.JsonObject;

public interface IKieRequestChangeListener {
	void objectChanged(JsonObject object);
}
