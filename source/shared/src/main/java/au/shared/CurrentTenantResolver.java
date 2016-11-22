package au.shared;

import java.io.Serializable;

public interface CurrentTenantResolver <T extends Serializable> {
    T getCurrentTenantId();
}