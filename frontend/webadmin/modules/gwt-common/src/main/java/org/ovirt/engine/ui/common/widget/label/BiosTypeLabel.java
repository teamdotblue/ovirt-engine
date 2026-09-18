package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class BiosTypeLabel extends ValueLabel<BiosType> {

    private final BiosTypeRenderer biosTypeRenderer;

    public BiosTypeLabel() {
        this(new BiosTypeRenderer());
    }

    public BiosTypeLabel(BiosTypeRenderer renderer) {
        super(renderer);
        this.biosTypeRenderer = renderer;
    }

    /** Forwards the effective compatibility version; see {@link BiosTypeRenderer}. */
    public void setCompatibilityVersion(Version compatibilityVersion) {
        biosTypeRenderer.setCompatibilityVersion(compatibilityVersion);
    }
}
