package org.ovirt.engine.core.bll.provider.storage;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateLibvirtSecretCommand extends LibvirtSecretCommandBase {

    @Inject
    private Instance<LibvirtSecretValidator> libvirtSecretValidatorInstance;

    public UpdateLibvirtSecretCommand(LibvirtSecretParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        LibvirtSecretValidator libvirtSecretValidator =
                libvirtSecretValidatorInstance.get().init(getParameters().getLibvirtSecret());
        return validate(libvirtSecretValidator.uuidExist())
                && validate(libvirtSecretValidator.valueNotEmpty())
                && validate(libvirtSecretValidator.providerExist());
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        libvirtSecretDao.update(getParameters().getLibvirtSecret());
        registerLibvirtSecret();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_LIBVIRT_SECRET : AuditLogType.USER_FAILED_TO_UPDATE_LIBVIRT_SECRET;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
