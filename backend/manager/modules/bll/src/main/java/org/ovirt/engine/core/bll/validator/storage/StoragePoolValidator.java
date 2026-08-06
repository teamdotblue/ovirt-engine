package org.ovirt.engine.core.bll.validator.storage;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

/**
 * Validate validation methods for storage pool handling
 */
public class StoragePoolValidator {

    @Inject
    private ClusterDao clusterDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private StoragePool storagePool;

    public StoragePoolValidator(StoragePool storagePool) {
        this.storagePool = storagePool;
    }

    public StoragePoolValidator() {
    }

    public StoragePoolValidator init(StoragePool storagePool) {
        this.storagePool = storagePool;
        return this;
    }

    protected ClusterDao getClusterDao() {
        return clusterDao;
    }

    public StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return storagePoolIsoMapDao;
    }

    public ValidationResult isNotLocalfsWithDefaultCluster() {
        if (storagePool.isLocal() && containsDefaultCluster()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_CLUSTER_CANNOT_BE_LOCALFS);
        }
        return ValidationResult.VALID;
    }

    protected boolean containsDefaultCluster() {
        List<Cluster> clusters = getClusterDao().getAllForStoragePool(storagePool.getId());
        boolean hasDefaultCluster = false;
        for (Cluster cluster : clusters) {
            if (cluster.getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultClusterID))) {
                hasDefaultCluster = true;
                break;
            }
        }
        return hasDefaultCluster;
    }

    public ValidationResult exists() {
        if (storagePool == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult existsAndUp() {
        ValidationResult existsResult = exists();
        if (!existsResult.isValid()) {
            return existsResult;
        }

        return isInStatus(StoragePoolStatus.Up);
    }

    public ValidationResult isAnyDomainInProcess() {
        List<StoragePoolIsoMap> poolIsoMaps = getStoragePoolIsoMapDao().getAllForStoragePool(storagePool.getId());

        for (StoragePoolIsoMap domainIsoMap : poolIsoMaps) {
            if (domainIsoMap.getStatus() != null && domainIsoMap.getStatus().isStorageDomainInProcess()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                        String.format("$status %1$s", domainIsoMap.getStatus()));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that the storage pool is in one of the supplied {@code statuses}
     */
    public ValidationResult isInStatus(StoragePoolStatus... statuses) {
        List<StoragePoolStatus> statusList = Arrays.asList(statuses);
        if (!statusList.contains(storagePool.getStatus())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
        }
        return ValidationResult.VALID;
    }

    /**
     * Validates that the storage pool is <strong>not</strong> in one of the supplied {@code statuses}
     */
    public ValidationResult isNotInStatus(StoragePoolStatus... statuses) {
        List<StoragePoolStatus> statusList = Arrays.asList(statuses);
        if (statusList.contains(storagePool.getStatus())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
        }
        return ValidationResult.VALID;
    }
}
