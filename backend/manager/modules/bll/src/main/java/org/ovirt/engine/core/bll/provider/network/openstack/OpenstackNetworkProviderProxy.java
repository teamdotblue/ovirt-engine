package org.ovirt.engine.core.bll.provider.network.openstack;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

public class OpenstackNetworkProviderProxy extends BaseNetworkProviderProxy<OpenstackNetworkProviderProperties> {

    @Inject
    private Instance<NetworkProviderValidator> networkProviderValidatorInstance;

    public OpenstackNetworkProviderProxy() {
    }

    public OpenstackNetworkProviderProxy init(Provider<OpenstackNetworkProviderProperties> provider) {
        setProvider(provider);
        return this;
    }

    @Override
    public ProviderValidator getProviderValidator() {
        return networkProviderValidatorInstance.get().createInstance(getProvider());
    }
}
