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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.prokopyl.commandtools.PlayerToolStore;
import com.google.common.io.Files;

abstract public class UsernameDictionary 
{
    static private final String MIGRATION_LIST_FILE_NAME = "migratedusers.txt";
    static private Map<String, UUID> migratedUsers;
    
    static public UUID getOldUser(String userName)
    {
        if(migratedUsers == null) LoadUserList();
        
        return migratedUsers.get(userName);
    }
    
    static public void saveUserList(Map<String, UUID> usersUUIDs) throws IOException
    {
        migratedUsers = usersUUIDs;
        
        File migrationListFile = new File(PlayerToolStore.getToolsDir(), MIGRATION_LIST_FILE_NAME);
        if(!migrationListFile.exists()) migrationListFile.createNewFile();
        BufferedWriter writer = Files.newWriter(migrationListFile, StandardCharsets.UTF_8);
        
        for(String key : migratedUsers.keySet())
        {
            writer.write(migratedUsers.get(key).toString() + " " + key + System.lineSeparator());
        }
        
        writer.close();
    }
    
    static private void LoadUserList()
    {
        migratedUsers = new HashMap<String, UUID>();
        
        File migrationListFile = new File(PlayerToolStore.getToolsDir(), MIGRATION_LIST_FILE_NAME);
        if(!migrationListFile.exists()) return;
        try
        {
            for(String line : Files.readLines(migrationListFile, StandardCharsets.UTF_8))
            {
                String[] values = line.split(" ");
                if(values.length != 2) continue;
                try
                {
                    migratedUsers.put(values[1], UUID.fromString(values[0]));
                }
                catch(IllegalArgumentException ex){}
            }
        }
        catch(IOException ex)
        {
            UUIDMigrator.logError("Cannot read migrated userlist file", ex);
        }
    }
}
