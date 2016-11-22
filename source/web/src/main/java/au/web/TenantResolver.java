package au.web;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.account.TenantContext;

import au.shared.CurrentTenantResolver;

@Component
public class TenantResolver implements CurrentTenantResolver<String> {

	public static final String TENANT_ID = "TENANT-01";
	protected static final Logger log = LoggerFactory.getLogger(TenantResolver.class);

	private static final ThreadLocal<String> currentTenant = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			InitialContext ctx;
			try {
				ctx = new InitialContext();

				Context envCtx = (Context) ctx.lookup("java:comp/env");
				TenantContext tenantContext = (TenantContext) envCtx.lookup("TenantContext");

				log.info(">> Tenant.Id={}", tenantContext.getTenant().getId());
				
				return tenantContext.getTenant().getId();
				
			} catch (NamingException e) {
				return "NOT_CURRENTLY_RUNNING_MULTI_-_TENANT"; 
			}
		}
	};

	public String getCurrentTenantId() {
		return currentTenant.get();
	}

}