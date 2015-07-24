package org.kie.eclipse.navigator.view.actions.dialogs;

import com.eclipsesource.json.JsonObject;

public interface IKieRequestValidator {
	public String isValid(JsonObject object);
}