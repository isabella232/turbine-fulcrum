package org.apache.fulcrum.security.torque.om.map;

import java.util.Date;
import java.math.BigDecimal;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.map.MapBuilder;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.map.TableMap;

/**
  *  This class was autogenerated by Torque on:
  *
  * [Sun Aug 27 22:32:46 CEST 2006]
  *
  */
public class TorqueDynamicUserDelegatesMapBuilder implements MapBuilder
{
    /**
     * The name of this class
     */
    public static final String CLASS_NAME =
        "org.apache.fulcrum.security.torque.om.map.TorqueDynamicUserDelegatesMapBuilder";

    /**
     * The database map.
     */
    private DatabaseMap dbMap = null;

    /**
     * Tells us if this DatabaseMapBuilder is built so that we
     * don't have to re-build it every time.
     *
     * @return true if this DatabaseMapBuilder is built
     */
    public boolean isBuilt()
    {
        return (dbMap != null);
    }

    /**
     * Gets the databasemap this map builder built.
     *
     * @return the databasemap
     */
    public DatabaseMap getDatabaseMap()
    {
        return this.dbMap;
    }

    /**
     * The doBuild() method builds the DatabaseMap
     *
     * @throws TorqueException
     */
    public void doBuild() throws TorqueException
    {
        dbMap = Torque.getDatabaseMap("fulcrum");

        dbMap.addTable("DYNAMIC_USER_DELEGATES");
        TableMap tMap = dbMap.getTable("DYNAMIC_USER_DELEGATES");

        tMap.setPrimaryKeyMethod("none");


              tMap.addForeignPrimaryKey(
                "DYNAMIC_USER_DELEGATES.DELEGATOR_USER_ID", new Integer(0) , "FULCRUM_USER" ,
                "USER_ID");
                    tMap.addForeignPrimaryKey(
                "DYNAMIC_USER_DELEGATES.DELEGATEE_USER_ID", new Integer(0) , "FULCRUM_USER" ,
                "USER_ID");
          }
}
