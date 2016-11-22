package au.model.jpa;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import au.shared.CurrentTenantResolver;

@SuppressWarnings("serial")
public class MultiTenantJpaTransactionManager extends JpaTransactionManager {

    @SuppressWarnings("rawtypes")
	@Autowired
    private CurrentTenantResolver tenantResolver;

    @Override
    protected void doBegin(final Object transaction, final TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        final EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(getEntityManagerFactory());
        final EntityManager em = emHolder.getEntityManager();
        final Serializable tenantId = tenantResolver.getCurrentTenantId();
        if (tenantId != null) {
            em.setProperty("eclipselink.tenant-id", tenantId);
        } else {
            
        }
    }
}