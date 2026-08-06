package org.ovirt.engine.core.bll.validator;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.QosDao;

public class CpuQosValidator extends QosValidator<CpuQos> {

    @Inject
    private CpuQosDao cpuQosDao;

    public CpuQosValidator(CpuQos qos) {
        super(qos);
    }

    public CpuQosValidator() {
    }

    public CpuQosValidator init(CpuQos qos) {
        super.init(qos);
        return this;
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return cpuQosDao;
    }

    @Override
    public ValidationResult requiredValuesPresent() {
        if (getQos().getCpuLimit() == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_MISSING_VALUES);
        }
        return ValidationResult.VALID;
    }
}
