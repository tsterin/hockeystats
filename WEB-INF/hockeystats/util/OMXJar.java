// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/OMXJar.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;


public class OMXJar
{
    private static final int FILE_READER_BUFFER_SIZE = 1024;

    private ByteArrayOutputStream manStream;
    private String fileName;
    private FileOutputStream jarFileStream;
    private Vector entries;

    private boolean verbose = false;

    private static final int CREATE_MODE = 0;
    private static final int READ_MANIFEST_MODE = 1;
    private static final int READ_MANIFEST_FOR_FILE_MODE = 2;
    private static final int WRITE_META_MODE = 10;
    private int mode = CREATE_MODE;

    private static final String META_FILE_NAME = "META.INF";


    public OMXJar(String options, String fileName) throws OMXJarException
    {
        debug("<constructor> called");

        handleOptions(options);
        this.fileName = fileName;
    }


    private void debug(String message)
    {
        //System.out.println(message);
    }


    private void handleOptions(String options) throws OMXJarException
    {
        debug("handleOptions called");
        if (options.indexOf("c") >= 0) {
            mode = CREATE_MODE;
        } else if (options.indexOf("r") >= 0) {
            mode = READ_MANIFEST_MODE;
        } else if (options.indexOf("m") >= 0) {
            mode = WRITE_META_MODE;
        } else {
            throw new OMXJarException("You did not specify a known mode. Known modes are: c, r, m.");
        }

        if (options.indexOf("f") < 0) {
            throw new OMXJarException("You must specify the file you want to operate on with the f option");
        }

        if (options.indexOf("v") >= 0) {
            verbose = true;
        }
    }


    public void go(String[] files) throws OMXJarException
    {
        debug("go called");
        switch (mode) {
            case CREATE_MODE:
                create(files);
                break;
            case READ_MANIFEST_MODE:
                if (files.length > 0) {
                    readManifest(files[0]);
                } else {
                    readManifest(null);
                }
                break;
            case WRITE_META_MODE:
                if (files.length > 0) {
                    writeMeta(new File(files[0]));
                } else {
                    writeMeta(null);
                }
                break;
        }
    }


    private File correctPath(File dir, File currentDir)
    {
        String temp = "";
        while (currentDir != null && !currentDir.getName().equalsIgnoreCase("java")) {
            temp = currentDir.getName() + System.getProperty("file.separator") + temp;
            currentDir = currentDir.getParentFile();
        }
        debug("Corrected path is: " + dir.getPath() + System.getProperty("file.separator") + temp);
        return new File(dir.getPath() + System.getProperty("file.separator") + temp);
    }


    private void writeMeta(File dir) throws OMXJarException
    {
        try {
            File file = new File(fileName);
            if (dir == null || ! dir.isDirectory()) {
                debug("Using parent");
                dir = file.getParentFile();
            }
            String currentDir = System.getProperty("user.dir");
            dir = correctPath(dir, new File(currentDir));
            MetaFile metaFile = new MetaFile(new File(dir, META_FILE_NAME));

            Hashtable ht = new Hashtable();
            ht.put("Name", file.getName());
            ht.put("UserName", System.getProperty("user.name"));
            ht.put("UserHomeDirectory", System.getProperty("user.home"));
            ht.put("UserWorkingDirectory", System.getProperty("user.dir"));
            ht.put("DateCompiled", (new Date()).toString());
            ht.put("JavaVersion", System.getProperty("java.version"));
            ht.put("CVSHeader", getHeader(file));

            metaFile.put(file.getName(), ht);
            metaFile.write();
        } catch (IOException e) {
            throw new OMXJarException("Got an IOException on use -- " + e);
        }
    }


    private String getHeader(File file) throws IOException
    {
        final String HEADER = "$Header";

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf(HEADER) >= 0) {
                br.close();
                int len = line.indexOf(HEADER) + HEADER.length() + 2;
                if (line.length() > len) {
                    return line.substring(line.indexOf(HEADER) + HEADER.length() + 2);
                } else {
                    return "Not checked into CVS yet";
                }
            }
        }

        br.close();
        return "No header found";
    }


    private class MetaFile
    {
        private File file;
        private BufferedReader reader;
        private Hashtable entries;

        private MetaFile(File file) throws IOException
        {
            this.file = file;
            entries = new Hashtable();
            initRead();
            process();
        }

        private void put(String key, Hashtable ht)
        {
            key = key.trim();
            if (key.endsWith(".java")) {
                key = key.substring(0, key.lastIndexOf(".java"));
            }
            debug("***Putting a new entry: " + key + ", value=" + ht);
            entries.put(key, ht);
        }

        private Hashtable get(String key)
        {
            if (key.endsWith(".class")) {
                key = key.substring(0, key.lastIndexOf(".class"));
            }
            debug("***Getting entry: " + key);
            return (Hashtable) entries.get(key);
        }

        private void initRead() throws IOException
        {
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
            }
        }

        private void write() throws IOException
        {
            debug("Writing MetaFile");
            FileWriter fw = new FileWriter(file);

            for (Enumeration e1 = entries.keys(); e1.hasMoreElements(); ) {
                String key1 = (String) e1.nextElement();
                Hashtable ht = (Hashtable) entries.get(key1);
                for (Enumeration e2 = ht.keys(); e2.hasMoreElements(); ) {
                    String key2 = (String) e2.nextElement();
                    fw.write(key2 + ": " + (String) ht.get(key2) + "\n");
                    debug("Writing: " + key2 + ": " + (String) ht.get(key2));
                }
                fw.write("-\n");
                debug("Writing: -");
            }
            fw.close();
        }

        private void process() throws IOException
        {
            if (reader != null) {
                Hashtable ht = new Hashtable();
                String line;
                while ((line = reader.readLine()) != null) {
                    debug("Line is: " + line);

                    if (line == null || line.trim().length() == 0 || line.trim().equals("-")) {
                        if (ht.get("Name") != null) {
                            put((String) ht.get("Name"), ht);
                            ht = new Hashtable();
                        } else {
                            debug("Found an extra line");
                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(line);
                        String key;
                        if (st.hasMoreTokens()) {
                            key = st.nextToken(":").trim();
                        } else {
                            key = "<unknown>";
                        }
                        String value;
                        if (st.hasMoreTokens()) {
                            value = st.nextToken("\n").trim();
                        } else {
                            value = "<unknown>";
                        }

                        ht.put(key, value);
                        debug("Adding key=" + key + ", value=" + value);
                    }
                }
            }
        }
    }


    private void readManifest(String entry) throws OMXJarException
    {
        debug("readManifest called");

        try {
            JarInputStream jis = new JarInputStream(new FileInputStream(fileName));
            Manifest manifest = jis.getManifest();
            if (manifest != null && entry != null) {
                Attributes attr = manifest.getAttributes(entry);
                if (attr != null) {
                    System.out.println("\nEntry " + entry + ":\n");

                    Attributes.Name[] keys = (Attributes.Name[]) attr.keySet().toArray(new Attributes.Name[0]);
                    for (int x = 0; x < keys.length; x++) {
                        System.out.println(keys[x] + ": " + attr.getValue(keys[x]));
                    }
                    System.out.println();

                } else {
                    System.out.println("Entry " + entry + " not found.");
                    System.out.println("Available main attributes are (" + manifest.getMainAttributes().keySet().size() + "):");

                    Attributes.Name[] keys = (Attributes.Name[]) manifest.getMainAttributes().keySet().toArray(new Attributes.Name[0]);
                    for (int x = 0; x < keys.length; x++) {
                        System.out.println(keys[x] + ": " + manifest.getMainAttributes().getValue(keys[x]));
                    }

                    System.out.println("\nAvailable entry attributes are (" + manifest.getMainAttributes().keySet().size() + "):");
                    Attributes.Name[] entries = (Attributes.Name[]) manifest.getEntries().keySet().toArray(new Attributes.Name[0]);
                    for (int x = 0; x < keys.length; x++) {
                        System.out.println("Key: " + keys[x]);
                        //System.out.println("Key: " + keys[x] + ", Value: " + manifest.getEntries().getAttributes(keys[x]));
                    }
                }

            } else if (manifest != null && entry == null) {
                debug("Writing manifest");
                manifest.write(System.out);
                debug("Done writing manifest");
            } else {
                throw new OMXJarException("No manifest found for this file");
            }

        } catch (FileNotFoundException e) {
            throw new OMXJarException("Could not open file -- " + e);
        } catch (IOException e) {
            throw new OMXJarException("Got an IOException on use -- " + e);
        }
    }


    private void create(String[] files) throws OMXJarException
    {
        debug("create called");
        entries = new Vector();

        try {
            manStream = new ByteArrayOutputStream();

            manStream.write("Manifest-Version: 1.0\n".getBytes());
            manStream.write(("Created-By: " + System.getProperty("java.version") + " (OfficeMax.com)\n\n").getBytes());

            for (int x = 0; x < files.length; x++) {
                handleDirectory(new File(files[x]));
            }

            completeCreate();

        } catch (IOException e) {
            throw new OMXJarException("Got an IOException on use -- " + e);
        }
    }


    private void completeCreate() throws IOException
    {
        debug("completeCreate called");

        jarFileStream = new FileOutputStream(fileName);
        Manifest manifest = new Manifest(new ByteArrayInputStream(manStream.toByteArray()));
        manStream.close();
        JarOutputStream jarOS = new JarOutputStream(jarFileStream, manifest);

        for (int x = 0; x < entries.size(); x++) {
            OMXJarEntry omxJE = (OMXJarEntry) entries.get(x);
            debug("Jarring: " + omxJE.getJarEntry().getName());
            jarOS.putNextEntry(omxJE.getJarEntry());
            jarOS.write(omxJE.getFileByteArray());
        }

        debug("done jarring");
        jarOS.close();
    }


    private void handleDirectory(File directory) throws FileNotFoundException, IOException
    {
        debug("handleDirectory called");
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            MetaFile metaFile = null;

            for (int x = 0; x < files.length ; x++) {
                if (files[x].getName().equalsIgnoreCase(META_FILE_NAME)) {
                    metaFile = new MetaFile(files[x]);
                }
            }

            for (int x = 0; x < files.length; x++) {
                if (files[x].isDirectory()) {
                    handleDirectory(files[x]);
                } else if (! files[x].getName().equalsIgnoreCase(META_FILE_NAME)) {
                    handleFile(files[x], metaFile);
                }
            }

        } else {
            handleFile(directory, null);
        }
    }


    private void handleFile(File file, MetaFile metaFile) throws FileNotFoundException, IOException
    {
        debug("handleFile called");
        Hashtable metaInfo = null;
        String name = file.getName();

        if (metaFile != null) {
            debug("MetaFile found, so getting metaInfo about this file");
            metaInfo = metaFile.get(name);
            if (metaInfo == null) {
                debug("MetaFile found, but this file was not found as an entry in it: " + name);
            }
        }

        if (verbose) {
            System.out.println("Storing: " + file.getPath());
        }

        manStream.write(("Name: " + file.getPath() + "\n").getBytes());
        if (metaInfo != null) {
            debug("Meta info found");
            manStream.write(("UserName: " + metaInfo.get("UserName") + "\n").getBytes());
            manStream.write(("UserHomeDirectory: " + metaInfo.get("UserHomeDirectory") + "\n").getBytes());
            manStream.write(("UserWorkingDirectory: " + metaInfo.get("UserWorkingDirectory") + "\n").getBytes());
            manStream.write(("DateCompiled: " + metaInfo.get("DateCompiled") + "\n").getBytes());
            manStream.write(("JavaVersion: " + metaInfo.get("JavaVersion") + "\n").getBytes());
            manStream.write(("CVSHeader: " + metaInfo.get("CVSHeader") + "\n").getBytes());
        } else {
            debug("Meta info not found");
            manStream.write(("MetaInfo: " + "Not found" + "\n").getBytes());
        }
        manStream.write("\n".getBytes());

        entries.add(new OMXJarEntry(new JarEntry(file.getPath()), file));
        debug("handleFile done");
    }


    private class OMXJarEntry
    {
        private JarEntry je;
        private File file;
        private boolean fileRead = false;
        private ByteArrayOutputStream baos;
        private CRC32 checkSum;


        private OMXJarEntry(JarEntry je, File file)
        {
            this.je = je;
            this.file = file;
        }


        private JarEntry getJarEntry()
        {
            return je;
        }


        private void readFile() throws IOException
        {
            if (! fileRead) {
                checkSum = new CRC32();
                baos = new ByteArrayOutputStream();
                FileInputStream fis = new FileInputStream(file);

                byte[] buffer = new byte[FILE_READER_BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    checkSum.update(buffer, 0, bytesRead);
                    baos.write(buffer, 0, bytesRead);
                }

                fileRead = true;
                fis.close();
            }
        }


        private long getCheckSum() throws IOException
        {
            readFile();
            return checkSum.getValue();
        }


        private byte[] getFileByteArray() throws IOException
        {
            readFile();
            return baos.toByteArray();
        }

    }


    public static class OMXJarException extends Exception
    {
        public OMXJarException(String message)
        {
            super(message);
        }
    }


    public static void main(String[] args) throws Exception
    {
        if (args.length >= 2) {
            OMXJar jar = new OMXJar(args[0], args[1]);

            Vector files = new Vector();
            for (int x = 2; x < args.length; x++) {
                files.add(args[x]);
            }

            jar.go((String[]) files.toArray(new String[0]));

        } else {
            System.out.println("--Unknown usage--");
            System.out.println();
            System.out.println("Usage 1: Jarring");
            System.out.println("java hockeystats.util.OMXJar c[v]f myfile.jar *");
            System.out.println();
            System.out.println("Usage 2: Read manifest for existing jar file");
            System.out.println("java hockeystats.util.OMXJar r[v]f myfile.jar");
            System.out.println();
            System.out.println("Usage 3: Write META.INF file for a package");
            System.out.println("java hockeystats.util.OMXJar m[v]f MyClassFile.java [Directory where you want META.INF]");
            System.out.println();
            System.out.println("Options:");
            System.out.println("c - create");
            System.out.println("r - read manifest");
            System.out.println("m - write META.INF file for package");
            System.out.println("v - verbose");
            System.out.println("f - file");
            System.out.println();
        }
    }
}