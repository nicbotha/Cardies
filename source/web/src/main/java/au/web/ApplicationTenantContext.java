package au.web;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import com.sap.cloud.account.Account;
import com.sap.cloud.account.InvalidTenantException;
import com.sap.cloud.account.Tenant;
import com.sap.cloud.account.TenantAlreadySetException;
import com.sap.cloud.account.TenantContext;

public class ApplicationTenantContext implements TenantContext {
	
	private final String tenant;
	
	public ApplicationTenantContext(String tenant) {
		this.tenant = tenant;
	}

	@Override
	public <V> V execute(String arg0, Callable<V> arg1) throws TenantAlreadySetException, InvalidTenantException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAccountName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Tenant> getSubscribedTenants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tenant getTenant() {
		Tenant t = new Tenant() {
			public com.sap.cloud.account.Account getAccount() {
				Account a = new Account() {

					@Override
					public Map<String, String> getAttributes() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Map<String, String> getAttributes(String arg0) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getCustomerId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getName() {
						// TODO Auto-generated method stub
						return null;
					}					
				};
				return a;
			}

			@Override
			public String getId() {
				return tenant;
			};
		};
		return t;
	}

	@Override
	public String getTenantId() {
		// TODO Auto-generated method stub
		return null;
	}

}
