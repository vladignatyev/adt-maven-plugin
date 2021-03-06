package com.yelbota.plugins.adt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @goal unpack
 * @threadSafe
 */
public class UnpackAdtMojo extends DependencyAdtMojo {

    protected File sdkDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File artifactFile = getAirSdkArtifact().getFile();
        File unpackDir = new File(System.getProperty("java.io.tmpdir"), "air_sdk_" + sdkVersion);

        List<String> unpackedFiles = unpackTo(unpackDir, artifactFile);
        setExecutable(unpackDir, unpackedFiles);

        sdkDirectory = unpackDir;
    }

    /**
     * Unpack files to directory.
     * @param unpackDir
     * @param artifactFile
     * @return
     * @throws MojoFailureException
     */
    public List<String> unpackTo(File unpackDir, File artifactFile) throws MojoFailureException {

        List<String> result = new ArrayList<String>();

        if (unpackDir.exists() && unpackDir.isDirectory()) {

            getLog().debug("AIR SDK already unpacked");

        } else {

            if (unpackDir.exists()) {
                unpackDir.delete();
            }

            unpackDir.mkdirs();

            try {

                ZipInputStream zip = new ZipInputStream(new FileInputStream(artifactFile));
                ZipEntry entry = zip.getNextEntry();
                byte[] buf = new byte[1024];

                while (entry != null) {

                    String entryName = entry.getName()
                            .replace('/', File.separatorChar)
                            .replace('\\', File.separatorChar);

                    File file = new File(unpackDir, entryName);
                    result.add(entryName);

                    if (entry.isDirectory()) {

                        if (!file.mkdirs())
                            break;

                        entry = zip.getNextEntry();
                        continue;
                    }

                    FileOutputStream fileOut = new FileOutputStream(file);

                    int n;
                    while ((n = zip.read(buf, 0, 1024)) > -1) {
                        fileOut.write(buf, 0, n);
                    }

                    fileOut.close();
                    zip.closeEntry();
                    entry = zip.getNextEntry();
                }

            } catch (IOException e) {
            }
        }

        return result;
    }

    /**
     * Looking for a bin folders, and makes content executable.
     * @param dir parent directory
     * @param files list of file paths
     */
    public void setExecutable(File dir, List<String> files) {

        for (String fileName : files) {

            File file = new File(dir, fileName);

            if (!file.isDirectory() && fileName.indexOf("bin") > -1)
                file.setExecutable(true);
        }
    }
}
