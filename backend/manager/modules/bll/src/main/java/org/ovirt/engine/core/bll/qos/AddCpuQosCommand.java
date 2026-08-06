package org.ovirt.engine.core.bll.qos;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class AddCpuQosCommand extends AddQosCommand<CpuQos, QosValidator<CpuQos>> {

    @Inject
    private Instance<CpuQosValidator> cpuQosValidatorInstance;

    public AddCpuQosCommand(QosParametersBase<CpuQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return cpuQosDao;
    }

    @Override
    protected QosValidator<CpuQos> getQosValidator(CpuQos qos) {
        return cpuQosValidatorInstance.get().init(qos);
    }

}
