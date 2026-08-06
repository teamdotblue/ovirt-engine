package org.ovirt.engine.core.bll.storage.disk.cinder;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public abstract class CinderQueryBase<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private ProviderDao providerDao;

    private OpenStackVolumeProviderProxy volumeProviderProxy;

    public CinderQueryBase(P parameters, EngineContext context) {
        super(parameters, context);
    }

    public OpenStackVolumeProviderProxy getVolumeProviderProxy() {
        if (volumeProviderProxy == null) {
            volumeProviderProxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(
                    getParameters().getId(), getUserID(), getParameters().isFiltered(), providerProxyFactory, storageDomainDao, providerDao);
        }
        return volumeProviderProxy;
    }
}
