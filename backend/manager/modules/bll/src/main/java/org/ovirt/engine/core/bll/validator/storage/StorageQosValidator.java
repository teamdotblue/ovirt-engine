package org.ovirt.engine.core.bll.validator.storage;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;

public class StorageQosValidator extends QosValidator<StorageQos> {

    @Inject
    private StorageQosDao storageQosDao;

    public StorageQosValidator(StorageQos qos) {
        super(qos);
    }

    public StorageQosValidator() {
    }

    public StorageQosValidator init(StorageQos qos) {
        super.init(qos);
        return this;
    }

    @Override
    protected QosDao<StorageQos> getQosDao() {
        return storageQosDao;
    }

    /*
     * Bytes and iops values are independent categories.
     * Setting one value leads to reseting the other two in the same
     * category to unlimited.
     * A non-zero total value cannot be used with non-zero read or
     * write value.
     */
    @Override
    public ValidationResult requiredValuesPresent() {
        if (missingCategoryValues(getQos().getMaxThroughput(),
                getQos().getMaxReadThroughput(),
                getQos().getMaxWriteThroughput())
                || missingCategoryValues(getQos().getMaxIops(),
                        getQos().getMaxReadIops(),
                        getQos().getMaxWriteIops())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_QOS_ILLEGAL_VALUES);
        }
        return ValidationResult.VALID;
    }

    private boolean missingCategoryValues(Integer total, Integer read, Integer write) {
        return isPositive(total) && (isPositive(read) || isPositive(write));
    }

    private boolean isPositive(Integer value) {
        return value != null && value > 0;
    }
}
