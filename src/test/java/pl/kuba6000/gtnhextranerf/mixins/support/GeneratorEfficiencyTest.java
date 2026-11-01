package pl.kuba6000.gtnhextranerf.mixins.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gregtech.api.enums.VoltageIndex;

class GeneratorEfficiencyTest {

    @Test
    void standardCurveStartsAtLvAndSlowsAfterHv() {
        assertEquals(20, GeneratorEfficiency.forTier(VoltageIndex.LV, VoltageIndex.LV));
        assertEquals(30, GeneratorEfficiency.forTier(VoltageIndex.MV, VoltageIndex.LV));
        assertEquals(40, GeneratorEfficiency.forTier(VoltageIndex.HV, VoltageIndex.LV));
        assertEquals(45, GeneratorEfficiency.forTier(VoltageIndex.EV, VoltageIndex.LV));
        assertEquals(50, GeneratorEfficiency.forTier(VoltageIndex.IV, VoltageIndex.LV));
    }

    @Test
    void plasmaCurveRestartsAtEv() {
        assertEquals(20, GeneratorEfficiency.forTier(VoltageIndex.EV, VoltageIndex.EV));
        assertEquals(30, GeneratorEfficiency.forTier(VoltageIndex.IV, VoltageIndex.EV));
        assertEquals(40, GeneratorEfficiency.forTier(VoltageIndex.LuV, VoltageIndex.EV));
        assertEquals(45, GeneratorEfficiency.forTier(VoltageIndex.ZPM, VoltageIndex.EV));
        assertEquals(50, GeneratorEfficiency.forTier(VoltageIndex.UV, VoltageIndex.EV));
    }
}
