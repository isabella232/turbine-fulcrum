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
public class TorqueUserMapBuilder implements MapBuilder
{
    /**
     * The name of this class
     */
    public static final String CLASS_NAME =
        "org.apache.fulcrum.security.torque.om.map.TorqueUserMapBuilder";

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

        dbMap.addTable("FULCRUM_USER");
        TableMap tMap = dbMap.getTable("FULCRUM_USER");

        tMap.setPrimaryKeyMethod(TableMap.ID_BROKER);

        tMap.setPrimaryKeyMethodInfo(tMap.getName());

              tMap.addPrimaryKey("FULCRUM_USER.USER_ID", new Integer(0) );
                        	          tMap.addColumn("FULCRUM_USER.LOGIN_NAME", "", 64 );
                                      	          tMap.addColumn("FULCRUM_USER.PASSWORD_VALUE", "", 16 );
                        }
}
