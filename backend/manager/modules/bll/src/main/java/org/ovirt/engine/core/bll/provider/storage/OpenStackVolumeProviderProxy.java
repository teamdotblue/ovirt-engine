package org.ovirt.engine.core.bll.provider.storage;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.openstack.CustomizedRESTEasyConnector;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.ConnectionForInitialize;
import com.woorea.openstack.cinder.model.ConnectionInfo;
import com.woorea.openstack.cinder.model.Limits;
import com.woorea.openstack.cinder.model.Snapshot;
import com.woorea.openstack.cinder.model.SnapshotForCreate;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.VolumeForUpdate;
import com.woorea.openstack.cinder.model.VolumeType;
import com.woorea.openstack.cinder.model.VolumeTypes;

public class OpenStackVolumeProviderProxy extends AbstractOpenStackStorageProviderProxy<Cinder, OpenStackVolumeProviderProperties, CinderProviderValidator> {

    @Inject
    private CINDERStorageHelper cinderStorageHelper;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private Instance<CinderProviderValidator> cinderProviderValidatorInstance;

    private static final String API_VERSION = "/v2/";

    public OpenStackVolumeProviderProxy(Provider<OpenStackVolumeProviderProperties> provider) {
        this.provider = provider;
    }

    public OpenStackVolumeProviderProxy() {
    }

    public OpenStackVolumeProviderProxy init(Provider<OpenStackVolumeProviderProperties> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    protected String getTestUrlPath() {
        return "/";
    }

    @Override
    public void onAddition() {
        Guid storageDomainId = addStorageDomain(StorageType.CINDER, StorageDomainType.Volume);
        Guid storagePoolId = provider.getAdditionalProperties().getStoragePoolId();
        if (storagePoolId != null && !storagePoolId.equals(Guid.Empty)) {
            attachStorageDomainToDataCenter(storageDomainId, storagePoolId);
        }
    }

    protected void attachStorageDomainToDataCenter(Guid storageDomainId, Guid storagePoolId) {
        cinderStorageHelper.attachCinderDomainToPool(storageDomainId, storagePoolId);
        cinderStorageHelper.activateCinderDomain(storageDomainId, storagePoolId);
    }

    @Override
    protected Cinder getClient() {
        if (client == null) {
            client = new Cinder(getProvider().getUrl() + API_VERSION, new CustomizedRESTEasyConnector());
            if (getProvider().isRequiringAuthentication()) {
                setClientTokenProvider(client);
            }
        }
        return client;
    }

    public String createSnapshot(SnapshotForCreate snapshotForCreate) {
        Snapshot retCinderSnapshot = getClient().snapshots().create(snapshotForCreate).execute();
        return retCinderSnapshot.getId();
    }

    public void deleteSnapshot(String snapshotId) {
        getClient().snapshots().delete(snapshotId).execute();
    }

    public String cloneVolumeFromSnapshot(VolumeForCreate volumeForCreate) {
        Volume retCinderVolume = getClient().volumes().create(volumeForCreate).execute();
        return retCinderVolume.getId();
    }

    public String createVolume(VolumeForCreate volumeForCreate) {
        Volume retCinderVolume = getClient().volumes().create(volumeForCreate).execute();
        return retCinderVolume.getId();
    }

    public void deleteVolume(String volumeId) {
        getClient().volumes().delete(volumeId).execute();
    }

    public void updateVolume(String volumeId, VolumeForUpdate volumeForUpdate) {
        getClient().volumes().update(volumeId, volumeForUpdate).execute();
    }

    public void extendVolume(String volumeId, int newSize) {
        getClient().volumes().extend(volumeId, newSize).execute();
    }

    public CinderConnectionInfo initializeConnectionForVolume(String volumeId, ConnectionForInitialize connectionForInitialize) {
        ConnectionInfo connectionInfo = getClient().volumes().initializeConnection(volumeId, connectionForInitialize).execute();
        CinderConnectionInfo cinderConnectionInfo = new CinderConnectionInfo();
        cinderConnectionInfo.setDriverVolumeType(connectionInfo.getDriverVolumeType());
        cinderConnectionInfo.setData(connectionInfo.getData());
        return cinderConnectionInfo;
    }

    public Volume getVolumeById(String id) {
        return getClient().volumes().show(id).execute();
    }

    public Snapshot getSnapshotById(String id) {
        return getClient().snapshots().show(id).execute();
    }

    public Limits getLimits() {
        return getClient().limits().list().execute();
    }

    public List<Volume> getVolumes() {
        return getClient().volumes().list(true).execute().getList();
    }

    public List<CinderVolumeType> getVolumeTypes() {
        ArrayList<CinderVolumeType> cinderVolumeTypes = new ArrayList<>();
        OpenStackRequest<VolumeTypes> listRequest = getClient().volumeTypes().list();

        VolumeTypes volumeTypes = listRequest.execute();
        for (VolumeType volumeType : volumeTypes) {
            CinderVolumeType cinderVolumeType = new CinderVolumeType(
                    volumeType.getId(), volumeType.getName(), volumeType.getExtraSpecs());
            cinderVolumeTypes.add(cinderVolumeType);
        }
        return cinderVolumeTypes;
    }

    @Override
    public void onRemoval() {
        List<StorageDomain> storageDomains =
                storageDomainDao.getAllByConnectionId(provider.getId());

        // Removing the static and dynamic storage domain entries
        StorageDomain storageDomainEntry = storageDomains.get(0);
        storageDomainDao.remove(storageDomainEntry.getId());
    }

    public static OpenStackVolumeProviderProxy getFromStorageDomainId(Guid storageDomainId,
            ProviderProxyFactory providerProxyFactory, StorageDomainStaticDao storageDomainStaticDao, ProviderDao providerDao) {
        StorageDomainStatic storageDomainStatic = storageDomainStaticDao.get(storageDomainId);
        if (storageDomainStatic != null) {
            return getProviderFromStorageDomainStatic(storageDomainStatic, providerProxyFactory, providerDao);
        }
        return null;
    }

    public static OpenStackVolumeProviderProxy getFromStorageDomainId(Guid storageDomainId,
            Guid userID,
            boolean isFiltered,
            ProviderProxyFactory providerProxyFactory, StorageDomainDao storageDomainDao,
            ProviderDao providerDao) {
        StorageDomain storageDomain = storageDomainDao.get(storageDomainId, userID, isFiltered);
        if (storageDomain != null) {
            Provider provider = providerDao.get(new Guid(storageDomain.getStorage()));
            return providerProxyFactory.create(provider);
        }
        return null;
    }

    private static OpenStackVolumeProviderProxy getProviderFromStorageDomainStatic(
            StorageDomainStatic storageDomainStatic, ProviderProxyFactory providerProxyFactory, ProviderDao providerDao) {
        Provider provider = providerDao.get(new Guid(storageDomainStatic.getStorage()));
        return providerProxyFactory.create(provider);
    }

    @Override
    public CinderProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = cinderProviderValidatorInstance.get().createInstance(provider);
        }
        return providerValidator;
    }
}
