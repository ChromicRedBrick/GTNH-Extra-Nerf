package pl.kuba6000.gtnhextranerf.components;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.ItemList;

/** Component identities and their UEV appearance sources, resolved only after GregTech initializes. */
public enum ComponentType {

    MOTOR("motor", () -> ItemList.Electric_Motor_UEV.get(1)),
    PISTON("piston", () -> ItemList.Electric_Piston_UEV.get(1)),
    PUMP("pump", () -> ItemList.Electric_Pump_UEV.get(1)),
    CONVEYOR("conveyor", () -> ItemList.Conveyor_Module_UEV.get(1)),
    ROBOT_ARM("robot_arm", () -> ItemList.Robot_Arm_UEV.get(1)),
    SENSOR("sensor", () -> ItemList.Sensor_UEV.get(1)),
    EMITTER("emitter", () -> ItemList.Emitter_UEV.get(1)),
    FIELD_GENERATOR("field_generator", () -> ItemList.Field_Generator_UEV.get(1));

    private final String id;
    private final Supplier<ItemStack> uevAppearance;

    ComponentType(String id, Supplier<ItemStack> uevAppearance) {
        this.id = id;
        this.uevAppearance = uevAppearance;
    }

    public String id() {
        return id;
    }

    ItemStack uevAppearance() {
        return uevAppearance.get();
    }
}
