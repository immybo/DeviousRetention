package model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * In charge of knowing all different types of entities, and being able to construct them.
 */
public class EntityManager {
    public enum UNIT {
        TEST_UNIT
    }

    public enum BUILDING {
        TEST_BUILDING
    }

    private Map<UNIT, Integer> unitCosts = new HashMap<>();
    private Map<BUILDING, Integer> buildingCosts = new HashMap<>();

    private Map<UNIT, Constructor<? extends Unit>> unitTypes = new HashMap<>();
    private Map<BUILDING, Constructor<? extends Building>> buildingTypes = new HashMap<>();

    private static EntityManager instance;

    public static void initialise() {
        instance = new EntityManager();
    }

    private EntityManager() {
        initUnits();
        initBuildings();
    }

    private void initUnits() {
        unitTypes = new HashMap<UNIT, Constructor<? extends Unit>>();
        registerUnit(UNIT.TEST_UNIT, getUnitConstructor(TestUnit.class), 100);
    }

    private void initBuildings() {
        buildingTypes = new HashMap<BUILDING, Constructor<? extends Building>>();
        registerBuilding(BUILDING.TEST_BUILDING, getBuildingConstructor(TestBuilding.class), 1000);
    }

    private void registerUnit(UNIT unitClass, Constructor<? extends Unit> unitConstructor, int cost) {
        unitTypes.put(unitClass, unitConstructor);
        unitCosts.put(unitClass, cost);
    }

    private void registerBuilding(BUILDING buildingClass, Constructor<? extends Building> buildingConstructor, int cost) {
        buildingTypes.put(buildingClass, buildingConstructor);
        buildingCosts.put(buildingClass, cost);
    }

    public static int getUnitCost(UNIT unitClass) {
        return instance.unitCosts.get(unitClass);
    }

    public static int getBuildingCost(BUILDING buildingClass) {
        return instance.buildingCosts.get(buildingClass);
    }

    private Constructor<? extends Unit> getUnitConstructor(Class<? extends Unit> unitClass) {
        try {
            // x, y and player number
            return unitClass.getConstructor(new Class[]{double.class, double.class, int.class});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to find constructor when initialising unit " + e);
        }
    }

    private Constructor<? extends Building> getBuildingConstructor(Class<? extends Building> buildingClass) {
        try {
            // x, y and player number
            return buildingClass.getConstructor(new Class[]{double.class, double.class, int.class});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to find constructor when initialising building " + e);
        }
    }

    public static Unit instantiate(UNIT unit, double x, double y, int playerNumber) {
        try {
            return instance.unitTypes.get(unit).newInstance(x, y, playerNumber);
        } catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new IllegalStateException("Unable to instantiate unit " + e);
        }
    }

    public static Building instantiate(BUILDING building, double x, double y, int playerNumber) {
        try {
            return instance.buildingTypes.get(building).newInstance(x, y, playerNumber);
        } catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new IllegalStateException("Unable to instantiate building " + e);
        }
    }
}