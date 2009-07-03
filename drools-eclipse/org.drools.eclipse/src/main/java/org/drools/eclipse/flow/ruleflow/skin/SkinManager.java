package org.drools.eclipse.flow.ruleflow.skin;

import java.util.HashMap;
import java.util.Map;

public final class SkinManager {
	
	private static final SkinManager INSTANCE = new SkinManager();

	private Map<String, SkinProvider> skinProviders = new HashMap<String, SkinProvider>();
	
	private SkinManager() {
		registerSkinProviders();
	}
	
	public static SkinManager getInstance() {
		return INSTANCE;
	}
	
	private void registerSkinProviders() {
		skinProviders.put("default", new DefaultSkinProvider());
		skinProviders.put("BPMN", new BPMNSkinProvider());
		skinProviders.put("BPMN2", new BPMN2SkinProvider());
	}
	
	public SkinProvider getSkinProvider(String type) {
		return skinProviders.get(type);
	}
	
}
