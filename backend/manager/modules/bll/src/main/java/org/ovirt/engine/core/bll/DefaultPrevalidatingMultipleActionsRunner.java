package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;


public class DefaultPrevalidatingMultipleActionsRunner extends PrevalidatingMultipleActionsRunner {

    public DefaultPrevalidatingMultipleActionsRunner(ActionType actionType,
            List<ActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    public DefaultPrevalidatingMultipleActionsRunner() {
    }

    public DefaultPrevalidatingMultipleActionsRunner createInstance(ActionType actionType, List<ActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        init(actionType, parameters, commandContext, isInternal);
        return this;
    }

}
