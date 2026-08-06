package org.ovirt.engine.core.bll.validator;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A validator for an {@link HasStoragePool} instance.
 */
public class HasStoragePoolValidator {

    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private Instance<StoragePoolValidator> storagePoolValidatorInstance;

    private HasStoragePool entity;
    private StoragePoolValidator spValidator;

    public HasStoragePoolValidator(HasStoragePool entity) {
        this.entity = entity;
    }

    public HasStoragePoolValidator() {
    }

    public HasStoragePoolValidator init(HasStoragePool entity) {
        this.entity = entity;
        return this;
    }

    /**
     * @return An error iff the data center to which the network belongs doesn't exist.
     */
    public ValidationResult storagePoolExists() {
        if (entity.getStoragePoolId() == null) {
            return ValidationResult.VALID;
        }
        return getStoragePoolValidator().exists();
    }

    private StoragePoolValidator getStoragePoolValidator() {
        if (spValidator == null) {
            spValidator = storagePoolValidatorInstance.get().init(storagePoolDao.get(entity.getStoragePoolId()));
        }
        return spValidator;
    }

}
