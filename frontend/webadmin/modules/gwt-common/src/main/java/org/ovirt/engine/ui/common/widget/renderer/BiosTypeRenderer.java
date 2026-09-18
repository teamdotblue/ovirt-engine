package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

/**
 * Renderer for BiosType Enum values.
 *
 * @param <E>
 *            Enum type.
 */
public class BiosTypeRenderer extends EnumRenderer<BiosType> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private ArchitectureType architectureType;

    // null -> do not mark anything; set by the surfaces that know the entity's
    // effective compatibility version (see ClusterEmulatedMachines.showI440fxDeprecatedMarker)
    private Version compatibilityVersion;

    private String defaultString;

    public BiosTypeRenderer() {
    }

    public BiosTypeRenderer(String defaultString) {
        this.defaultString = defaultString;
    }

    @Override
    public String render(BiosType biosType) {
        if (biosType == null && defaultString != null) {
            return defaultString;
        }

        if (biosType == null) {
            return "";
        }

        if (architectureType != null && BiosType.I440FX_SEA_BIOS.equals(biosType)) {
            if (ArchitectureType.ppc.equals(architectureType.getFamily())) {
                return constants.ppcChipset();
            } else if (ArchitectureType.s390x.equals(architectureType.getFamily())) {
                return constants.s390xChipset();
            }
        }
        String rendered = super.render(biosType);
        if (BiosType.I440FX_SEA_BIOS.equals(biosType)
                && ClusterEmulatedMachines.showI440fxDeprecatedMarker(compatibilityVersion)) {
            rendered = rendered + " " + constants.emulatedMachineDeprecatedSuffix(); //$NON-NLS-1$
        }
        return rendered;
    }

    public void setArchitectureType(ArchitectureType architectureType) {
        this.architectureType = architectureType;
    }

    public ArchitectureType getArchitectureType() {
        return architectureType;
    }

    /**
     * Effective compatibility version used to decide whether i440fx is marked as
     * deprecated. Leave unset (null) for surfaces that cannot know it: they then show
     * the plain chipset name.
     */
    public void setCompatibilityVersion(Version compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }
}
