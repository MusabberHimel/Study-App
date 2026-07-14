package com.musabber.pomofocus.util

import android.content.Context
import android.content.Intent
import com.musabber.pomofocus.data.local.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupHelper {

    /**
     * Packages the Room database files and the DataStore file into a single compressed ZIP stream.
     */
    fun exportBackup(context: Context, outStream: OutputStream): Boolean {
        try {
            // Force checkpoint to flush write-ahead logs (WAL) to the primary database file
            AppDatabase.getDatabase(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val filesToBackup = mutableListOf<File>()
        
        val dbFile = context.getDatabasePath("pomofocus_database")
        val dbWal = context.getDatabasePath("pomofocus_database-wal")
        val dbShm = context.getDatabasePath("pomofocus_database-shm")
        val prefsFile = File(context.filesDir, "datastore/pomofocus_preferences.preferences_pb")

        if (dbFile.exists()) filesToBackup.add(dbFile)
        if (dbWal.exists()) filesToBackup.add(dbWal)
        if (dbShm.exists()) filesToBackup.add(dbShm)
        if (prefsFile.exists()) filesToBackup.add(prefsFile)

        if (filesToBackup.isEmpty()) return false

        return try {
            ZipOutputStream(outStream).use { zos ->
                for (file in filesToBackup) {
                    val entryName = if (file.absolutePath.contains("datastore")) {
                        "datastore/${file.name}"
                    } else {
                        file.name
                    }
                    zos.putNextEntry(ZipEntry(entryName))
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Unpacks the backup archive to a temporary directory, safety checks files, closes the active
     * Room connection, replaces database/preference files, and cleans up.
     */
    fun importBackup(context: Context, inputStream: InputStream): Boolean {
        try {
            // Close the database safely to unlock files
            AppDatabase.getDatabase(context).close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val tempDir = File(context.cacheDir, "backup_temp")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        try {
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(tempDir, entry.name)
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val extractedDb = File(tempDir, "pomofocus_database")
            val extractedPrefs = File(tempDir, "datastore/pomofocus_preferences.preferences_pb")

            if (!extractedDb.exists() || !extractedPrefs.exists()) {
                tempDir.deleteRecursively()
                return false
            }

            val dbFile = context.getDatabasePath("pomofocus_database")
            val dbWal = context.getDatabasePath("pomofocus_database-wal")
            val dbShm = context.getDatabasePath("pomofocus_database-shm")
            val prefsFile = File(context.filesDir, "datastore/pomofocus_preferences.preferences_pb")

            // Delete current files to overwrite cleanly
            dbFile.delete()
            if (dbWal.exists()) dbWal.delete()
            if (dbShm.exists()) dbShm.delete()
            if (prefsFile.exists()) prefsFile.delete()

            // Copy main db
            extractedDb.copyTo(dbFile, overwrite = true)
            
            // Copy auxiliary database journaling files if they exist
            val extractedWal = File(tempDir, "pomofocus_database-wal")
            if (extractedWal.exists()) {
                extractedWal.copyTo(dbWal, overwrite = true)
            }
            val extractedShm = File(tempDir, "pomofocus_database-shm")
            if (extractedShm.exists()) {
                extractedShm.copyTo(dbShm, overwrite = true)
            }

            // Copy preferences
            prefsFile.parentFile?.mkdirs()
            extractedPrefs.copyTo(prefsFile, overwrite = true)

            tempDir.deleteRecursively()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            tempDir.deleteRecursively()
            return false
        }
    }

    /**
     * Triggers a graceful, clean application restart.
     */
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        System.exit(0)
    }
}
