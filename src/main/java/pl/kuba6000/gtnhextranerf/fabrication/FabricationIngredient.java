package pl.kuba6000.gtnhextranerf.fabrication;

import pl.kuba6000.gtnhextranerf.components.ComponentType;

/** Stable identities for the fictional part assigned to each UQV component. */
public enum FabricationIngredient {

    UNIDENTIFIED_FLYING_ROTOR("unidentified_flying_rotor", ComponentType.MOTOR),
    NEGATIVE_VOLUME_CHAMBER("negative_volume_chamber", ComponentType.PUMP),
    MISSING_RETURN_BELT("missing_return_belt", ComponentType.CONVEYOR),
    IMMOVABLE_TELESCOPIC_ROD("immovable_telescopic_rod", ComponentType.PISTON),
    UNOBSERVED_PHOTON_SOURCE("unobserved_photon_source", ComponentType.EMITTER),
    UNCERTAINTY_DETECTOR("uncertainty_detector", ComponentType.SENSOR),
    UNIDENTIFIED_HELPING_HAND("unidentified_helping_hand", ComponentType.ROBOT_ARM),
    UNDEFINED_FIELD_BOUNDARY("undefined_field_boundary", ComponentType.FIELD_GENERATOR);

    private final String id;
    private final ComponentType component;

    FabricationIngredient(String id, ComponentType component) {
        this.id = id;
        this.component = component;
    }

    public String id() {
        return id;
    }

    public ComponentType component() {
        return component;
    }
}
