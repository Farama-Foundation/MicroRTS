/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional;

import ai.abstraction.pathfinding.PathFinding;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class ConditionalBiggerThen extends AbstractConditional {

    public ConditionalBiggerThen(int param1, int param2) {
        super(param1, param2);
        this.typeOfParam = 0;
    }

    public ConditionalBiggerThen(BigDecimal paramB1, BigDecimal paramB2) {
        super(paramB1, paramB2);
        this.typeOfParam = 1;
    }

    public ConditionalBiggerThen(Object ob1, Object ob2) {
        super(ob1, ob2);
        this.typeOfParam = 2;
    }

    public ConditionalBiggerThen(String function, List lParam1, int param1) {
        super(function, lParam1, param1);
        this.typeOfParam = 3;
    }

    public ConditionalBiggerThen(String function, List lParam1) {
        super(function, lParam1);
        this.typeOfParam = 6;
    }

    public ConditionalBiggerThen(String function, List lParam1, BigDecimal paramB1) {
        super(function, lParam1, paramB1);
        this.typeOfParam = 4;
    }

    public ConditionalBiggerThen(String function, List lParam1, String function2, List lParam2) {
        super(function, lParam1, function2, lParam2);
        this.typeOfParam = 5;
    }

    @Override
    public boolean runConditional(GameState game, int player, PlayerAction currentPlayerAction, 
                                        PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
