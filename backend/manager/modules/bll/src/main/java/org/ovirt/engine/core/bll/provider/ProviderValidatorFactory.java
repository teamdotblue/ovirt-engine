package org.ovirt.engine.core.bll.provider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@Dependent
public class ProviderValidatorFactory {

    @Inject
    private ProviderDao providerDao;

    public ProviderValidator createValidator(Provider<?> provider) {
        return new ProviderValidator(provider) {
            @Override
            public ProviderDao getProviderDao() {
                return providerDao;
            }
        };
    }
}
