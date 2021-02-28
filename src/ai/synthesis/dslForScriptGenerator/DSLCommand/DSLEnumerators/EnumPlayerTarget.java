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
public enum EnumPlayerTarget {
    Ally(0), Enemy(1);

    private final int code;

    EnumPlayerTarget(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumPlayerTarget byCode(int codigo) {
        for (EnumPlayerTarget t : EnumPlayerTarget.values()) {
            if (codigo == t.code()) {
                return t;
            }
        }
        throw new IllegalArgumentException("invalid code!");
    }

    public static EnumPlayerTarget byName(String name) {
        if (name.equals("Ally")) {
            return EnumPlayerTarget.Ally;
        } else if (name.equals("Enemy")) {
            return EnumPlayerTarget.Enemy;
        } 
        throw new IllegalArgumentException("invalid name!");
    }

}
