package au.model.bootstrap;

import org.springframework.stereotype.Component;

import au.shared.CurrentTenantResolver;

@Component
public class TestTenantResolver implements CurrentTenantResolver<String> {

	public static final String TENANT_ID = "TENANT-01";
	
	private static final ThreadLocal<String> currentTenant = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return TENANT_ID;
		}
	};

	public String getCurrentTenantId() {
		return currentTenant.get();
	}

}
