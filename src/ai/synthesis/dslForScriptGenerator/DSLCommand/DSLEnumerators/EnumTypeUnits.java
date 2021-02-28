/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators;

/**
 *
 * @author rubens
 */
public enum EnumTypeUnits {
    Resource(0), Base(1), Barracks(2), Worker(3), Light(4), Heavy(5), Ranged(6);

    private final int code;

    EnumTypeUnits(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumTypeUnits byCode(int codigo) {
        for (EnumTypeUnits t : EnumTypeUnits.values()) {
            if (codigo == t.code()) {
                return t;
            }
        }
        throw new IllegalArgumentException("invalid code!");
    }

    public static EnumTypeUnits byName(String name) {
        if (name.equals("Resource")) {
            return EnumTypeUnits.Resource;
        } else if (name.equals("Base")) {
            return EnumTypeUnits.Base;
        } else if (name.equals("Barracks")) {
            return EnumTypeUnits.Barracks;
        }else if (name.equals("Worker")) {
            return EnumTypeUnits.Worker;
        }else if (name.equals("Light")) {
            return EnumTypeUnits.Light;
        }else if (name.equals("Heavy")) {
            return EnumTypeUnits.Heavy;
        }else if (name.equals("Ranged")) {
            return EnumTypeUnits.Ranged;
        }
        throw new IllegalArgumentException("invalid name!");
    }

}
