/*
 * Copyright (C) 2014 ProkopyL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.prokopyl.commandtools.migration;

abstract public class UUIDMigrator 
{
    static private Thread migratorThread;
    
    static public void Migrate()
    {
        if(migratorThread != null && migratorThread.isAlive())
        {
            logError("Migration is already running.");
            return;
        }
        migratorThread = new Thread(new UUIDMigratorWorker());
        logInfo("Migration started.");
        migratorThread.start();
    }
    
    static public void WaitForMigration()
    {
        logInfo("Waiting for migration to finish ...");
        if(migratorThread != null && migratorThread.isAlive())
        {
            try
            {
                migratorThread.join();
            }
            catch(InterruptedException ex)
            {
                logError("Migration thread has been interrupted while wating to finish. It may not have ended correctly.");
            }
        }
    }
    
    static public void logInfo(String message)
    {
        System.out.println("[CommandTools-Migration][INFO] " + message);
    }
    
    static public void logError(String message)
    {
        System.err.println("[CommandTools-Migration][ERROR] " + message);
    }
    
    static public void logError(String message, Exception ex)
    {
        logError(message + " : " + ex.getMessage());
    }
}
