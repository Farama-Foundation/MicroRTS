/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumTypeUnits;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rubens
 */
public abstract class TypeConcrete implements IType{
    List<EnumTypeUnits> selectedTypes;

    public TypeConcrete() {
        selectedTypes = new ArrayList<>();
    }
    
    public static IType getTypeConstruction(){
        ConstructionTypeParam tReturn = new ConstructionTypeParam();
        tReturn.addType(EnumTypeUnits.Base);
        tReturn.addType(EnumTypeUnits.Barracks);
        return tReturn;
    }
    
    public static IType getTypeBase(){
        ConstructionTypeParam tReturn = new ConstructionTypeParam();
        tReturn.addType(EnumTypeUnits.Base);
        return tReturn;
    }
    
    public static IType getTypeBarracks(){
        ConstructionTypeParam tReturn = new ConstructionTypeParam();
        tReturn.addType(EnumTypeUnits.Barracks);
        return tReturn;
    }
    
    
    public static IType getTypeUnits(){
        UnitTypeParam tReturn = new UnitTypeParam();
        tReturn.addType(EnumTypeUnits.Heavy);
        tReturn.addType(EnumTypeUnits.Light);
        tReturn.addType(EnumTypeUnits.Ranged);
        tReturn.addType(EnumTypeUnits.Worker);
        return tReturn;
    }
    
    public static IType getTypeHeavy(){
        UnitTypeParam tReturn = new UnitTypeParam();
        tReturn.addType(EnumTypeUnits.Heavy);
        return tReturn;
    }
    
    public static IType getTypeLight(){
        UnitTypeParam tReturn = new UnitTypeParam();
        tReturn.addType(EnumTypeUnits.Light);
        return tReturn;
    }
    
    public static IType getTypeRanged(){
        UnitTypeParam tReturn = new UnitTypeParam();
        tReturn.addType(EnumTypeUnits.Ranged);
        return tReturn;
    }
    
    public static IType getTypeWorker(){
        UnitTypeParam tReturn = new UnitTypeParam();
        tReturn.addType(EnumTypeUnits.Worker);
        return tReturn;
    }
    
}
