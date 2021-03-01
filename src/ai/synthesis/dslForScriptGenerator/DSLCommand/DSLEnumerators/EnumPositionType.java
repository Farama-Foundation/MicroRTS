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
public enum EnumPositionType {
    Left(3), Right(1), Up(0), Down(2), EnemyDirection(4);
    
    private final int code;
    
    EnumPositionType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }
    
    public static EnumPositionType byCode(int codigo) {
        for (EnumPositionType t : EnumPositionType.values()) {
            if (codigo == t.code()) {
                return t;
            }
        }
        throw new IllegalArgumentException("invalid code!");
    }

    public static EnumPositionType byName(String name) {
        if (name.equals("Left")) {
            return EnumPositionType.Left;
        } else if (name.equals("Right")) {
            return EnumPositionType.Right;
        } else if (name.equals("Up")) {
            return EnumPositionType.Up;
        }else if (name.equals("Down")) {
            return EnumPositionType.Down;
        }else if (name.equals("EnemyDir")) {
            return EnumPositionType.EnemyDirection;
        }
        throw new IllegalArgumentException("invalid name!");
    }
    
}
