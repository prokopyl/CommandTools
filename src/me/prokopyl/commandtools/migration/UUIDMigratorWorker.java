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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.prokopyl.commandtools.PlayerToolStore;
import me.prokopyl.commandtools.ToolManager;

public class UUIDMigratorWorker implements Runnable
{
    static private final int MOJANG_USERNAMES_BY_REQUEST = 100;
    static private final String BACKUP_UUID_DIRNAME = "Backup_UUIDs";
    static private final String BACKUP_NAME_DIRNAME = "Backup_Names";
    
    private final ArrayList<String> filesToMigrate = new ArrayList<String>();
    private Map<String, UUID> usersUUIDs;
    private final ArrayList<OldPlayerToolStore> toolStores = new ArrayList<OldPlayerToolStore>();
    
    @Override
    public void run() 
    {
        try
        {
            if(!spotFilesToMigrate()) return;
            if(checkForExistingBackups()) return;
            fetchUUIDs();
            if(!checkMissingUUIDs()) return;
            backupToolStores();
            loadOldToolStores();
        }
        catch(Exception ex)
        {
            UUIDMigrator.logError("Error while preparing migration", ex);
            UUIDMigrator.logError("Aborting migration. No change has been made.");
            Logger.getLogger(UUIDMigratorWorker.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        try
        {
            mergeToolStores();
            saveChanges();
            cleanup();
            UUIDMigrator.logInfo("Migration completed successfuly !");
            UUIDMigrator.logInfo("You can now play with your CommandTools like before. :-)");
        }
        catch(Exception ex)
        {
            UUIDMigrator.logError("Error while migrating", ex);
            UUIDMigrator.logError("Aborting migration. Some changes may already have been made.");
            UUIDMigrator.logError("Before trying to migrate again, you must recover player files from the backups, and then move the backups away from the plugin directory to avoid overwriting them.");
            
            Logger.getLogger(UUIDMigratorWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* ****** Actions ***** */
    
    private boolean spotFilesToMigrate()
    {
        UUIDMigrator.logInfo("Looking for configuration files to migrate ...");
        File toolsDir = PlayerToolStore.getToolsDir();
        for(File file : toolsDir.listFiles())
        {
            int extensionPosition = file.getName().lastIndexOf('.');
            if(extensionPosition <= 0) continue;
            String fileName = file.getName().substring(0, extensionPosition);
            if(!isUUID(fileName)) filesToMigrate.add(fileName);
        }
        
        if(filesToMigrate.isEmpty())
        {
            UUIDMigrator.logInfo("There is nothing to migrate. Stopping.");
        }
        else
        {
            UUIDMigrator.logInfo("Done. Found " + filesToMigrate.size() + " files to migrate.");
        }
        
        return !filesToMigrate.isEmpty();
    }
    
    private boolean checkForExistingBackups()
    {
        File toolsDir = PlayerToolStore.getToolsDir();
        File backupUUIDDir = new File(toolsDir, BACKUP_UUID_DIRNAME);
        File backupNameDir = new File(toolsDir, BACKUP_NAME_DIRNAME);
        
        boolean backupsExist = false;
        
        if(backupNameDir.exists() && backupNameDir.listFiles().length == 0)
        {
            backupsExist = true;
        }
        else if(backupUUIDDir.exists() && backupUUIDDir.listFiles().length == 0)
        {
            backupsExist = true;
        }
        
        if(backupsExist)
        {
            UUIDMigrator.logError("Backup directories already exists.");
            UUIDMigrator.logError("This means that a migration has already been done, or may not have ended well.");
            UUIDMigrator.logError("To start a new migration, you must move away the backup directories so they are not overwritten.");
        }
            
        return backupsExist;
            
    }
    
    private void fetchUUIDs() throws IOException, InterruptedException
    {
        UUIDMigrator.logInfo("Fetching UUIDs from Mojang ...");
        try
        {
            usersUUIDs = UUIDFetcher.fetch(filesToMigrate, MOJANG_USERNAMES_BY_REQUEST);
        }
        catch(IOException ex)
        {
            UUIDMigrator.logError("An error occured while fetching the UUIDs from Mojang", ex);
            throw ex;
        }
        catch(InterruptedException ex)
        {
            UUIDMigrator.logError("The migration worker has been interrupted", ex);
            throw ex;
        }
        UUIDMigrator.logInfo("Fetching done. " + usersUUIDs.size() + " UUIDs have been retreived.");
    }
    
    private boolean checkMissingUUIDs()
    {
        if(usersUUIDs.size() == filesToMigrate.size()) return true;
        UUIDMigrator.logInfo("Mojang did not find UUIDs for all the registered players.");
        UUIDMigrator.logInfo("This means some of the users do not actually exist, or they have changed names before migrating.");
        
        if(usersUUIDs.size() <= 0)
        {
            UUIDMigrator.logInfo("Mojang could not find any of the registered players.");
            UUIDMigrator.logInfo("There is nothing to migrate. Stopping.");
            return false;
        }
        
        ArrayList<String> missingUsers = new ArrayList<String>();
        
        for(String user : filesToMigrate)
        {
            if(!usersUUIDs.containsKey(user)) missingUsers.add(user);
        }
        
        UUIDMigrator.logInfo("Here are the missing players : " + String.join(",", missingUsers));
        return true;
    }
    
    private void backupToolStores() throws IOException
    {
        UUIDMigrator.logInfo("Backuping tools before migrating ...");
        File toolsDir = PlayerToolStore.getToolsDir();
        File backupNameDir = new File(toolsDir, BACKUP_NAME_DIRNAME);
        File backupUUIDDir = new File(toolsDir, BACKUP_UUID_DIRNAME);
        
        if(!backupNameDir.exists()) backupNameDir.mkdir();
        if(!backupUUIDDir.exists()) backupUUIDDir.mkdir();
        
        File sourceFile;
        File destinationFile;
        
        //Backuping name-based files :
        for(String fileName : filesToMigrate)
        {
            fileName = fileName + ".yml";
            sourceFile = new File(toolsDir, fileName);
            destinationFile = new File(backupNameDir, fileName);
            verifiedBackupCopy(sourceFile, destinationFile);
        }
        
        //Backuping UUID-based files :
        String fileName;
        for(UUID playerUUID : usersUUIDs.values())
        {
            fileName = playerUUID.toString() + ".yml";
            sourceFile = new File(toolsDir, fileName);
            if(!sourceFile.exists()) continue;
            destinationFile = new File(backupUUIDDir, fileName);
            verifiedBackupCopy(sourceFile, destinationFile);
        }
        
        UUIDMigrator.logInfo("Backup complete.");
    }
    
    private void loadOldToolStores()
    {
        UUIDMigrator.logInfo("Loading old players tools...");
        File toolsDir = PlayerToolStore.getToolsDir();
        File storeFile;
        
        for(String fileName : filesToMigrate)
        {
            if(!usersUUIDs.containsKey(fileName)) continue;
            storeFile = new File(toolsDir, fileName + ".yml");
            toolStores.add(new OldPlayerToolStore(storeFile, fileName));
        }
        
        UUIDMigrator.logInfo("Loading complete. " + toolStores.size() + " players loaded.");
    }
    
    private void mergeToolStores()
    {
        UUIDMigrator.logInfo("Merging tools with existing ...");
        for(OldPlayerToolStore toolStore : toolStores)
        {
            toolStore.mergeWithExistingToolStore(usersUUIDs.get(toolStore.getUserName()));
        }
    }
    
    private void saveChanges()
    {
        UUIDMigrator.logInfo("Saving changes ...");
        ToolManager.save();
    }
    
    private void cleanup()
    {
        UUIDMigrator.logInfo("Cleaning up old data files...");
        File toolsDir = PlayerToolStore.getToolsDir();
        File storeFile;
        for(String fileName : filesToMigrate)
        {
            storeFile = new File(toolsDir, fileName + ".yml");
            storeFile.delete();
        }
    }
    
    /* ****** Utils ***** */
    
    static private boolean isUUID(String str)
    {
        try
        {
            UUID.fromString(str);
        }
        catch(IllegalArgumentException ex)
        {
            return false;
        }
        return true;
    }
    
    static private void verifiedBackupCopy(File sourceFile, File destinationFile) throws IOException
    {
        long sourceSize = sourceFile.length();
        String sourceCheckSum = fileCheckSum(sourceFile, "SHA1");
        
        Path sourcePath = Paths.get(sourceFile.getAbsolutePath());
        Path destinationPath = Paths.get(destinationFile.getAbsolutePath());
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
        long destinationSize = destinationFile.length();
        String destinationCheckSum = fileCheckSum(destinationFile, "SHA1");
        
        if(sourceSize != destinationSize || !sourceCheckSum.equals(destinationCheckSum))
        {
            throw new IOException("Backup copy failed : source and destination files ("+sourceFile.getName()+") differs after copy.");
        }
        
    }
    
    static private String fileCheckSum(File file, String algorithmName) throws IOException
    {
        MessageDigest instance;
        try
        {
            instance = MessageDigest.getInstance(algorithmName);
        }
        catch(NoSuchAlgorithmException ex)
        {
            throw new IOException("Could not check file integrity because of NoSuchAlgorithmException : " + ex.getMessage());
        }
        
        FileInputStream inputStream = new FileInputStream(file);
        
        byte[] data = new byte[1024];
        int read = 0;
        
        while((read = inputStream.read(data)) != -1)
        {
            instance.update(data);
        }
        
        byte[] hashBytes = instance.digest();
        
        StringBuilder buffer = new StringBuilder();
        char hexChar;
        for(int i = 0; i < hashBytes.length; i++)
        {
            hexChar = Integer.toHexString((hashBytes[i] & 0xff) + 0x100).charAt(0);
            buffer.append(hexChar);
        }
        
        return buffer.toString();
    }

}
