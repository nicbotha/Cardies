package au.web.service.cmis;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

public class CMISSessionManager {
	
	protected final String UNIQUE_NAME;
	protected final String SECRET_KEY;
	
	private Session session;
	
	protected EcmService cmisSvc;
	
	@Autowired
	@Qualifier("JndiObjectFactoryBean")
	protected JndiObjectFactoryBean jndiFactory;
	
	protected final Logger log = LoggerFactory.getLogger(CMISSessionManager.class);

	public CMISSessionManager(String uniqueName, String secretKey) {
		this.UNIQUE_NAME = uniqueName;
		this.SECRET_KEY = secretKey;
	}
	
	public synchronized Session getSession() {
		if(session == null) {
			session = openCmisSession();
		}
		return session;
	}	
	
	protected Session openCmisSession() {
		if(cmisSvc == null) {
			cmisSvc = (EcmService) jndiFactory.getObject();
		}
		try {
			return cmisSvc.connect(this.UNIQUE_NAME, this.SECRET_KEY);
		} catch (CmisObjectNotFoundException e) {
			RepositoryOptions options = new RepositoryOptions();
			options.setUniqueName(this.UNIQUE_NAME);
			options.setRepositoryKey(this.SECRET_KEY);
			options.setVisibility(Visibility.PROTECTED);
			cmisSvc.createRepository(options);
			return cmisSvc.connect(this.UNIQUE_NAME, this.SECRET_KEY);
		}
	}	
	
}
