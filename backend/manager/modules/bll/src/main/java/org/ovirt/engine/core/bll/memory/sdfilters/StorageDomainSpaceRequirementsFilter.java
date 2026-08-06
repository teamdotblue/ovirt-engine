package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.function.Predicate;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.memory.MemoryDisks;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;


public class StorageDomainSpaceRequirementsFilter implements Predicate<StorageDomain> {

    private MemoryStorageHandler memoryStorageHandler;
    private MemoryDisks memoryDisks;

    @Inject
    private Instance<StorageDomainValidator> storageDomainValidatorInstance;

    public StorageDomainSpaceRequirementsFilter(MemoryStorageHandler memoryStorageHandler, MemoryDisks memoryDisks) {
        this.memoryStorageHandler = memoryStorageHandler;
        this.memoryDisks = memoryDisks;
    }

    public StorageDomainSpaceRequirementsFilter() {
    }

    public StorageDomainSpaceRequirementsFilter init(MemoryStorageHandler memoryStorageHandler, MemoryDisks memoryDisks) {
        this.memoryStorageHandler = memoryStorageHandler;
        this.memoryDisks = memoryDisks;
        return this;
    }

    @Override
    public boolean test(StorageDomain storageDomain) {
        memoryStorageHandler.updateDisksStorage(storageDomain, memoryDisks);
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
        return storageDomainValidator.isDomainWithinThresholds().isValid() &&
                storageDomainValidator.hasSpaceForClonedDisks(memoryDisks.asList()).isValid();
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return storageDomainValidatorInstance.get().createInstance(storageDomain);
    }
}
