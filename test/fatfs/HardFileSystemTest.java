package fatfs;

import drives.Device;
import fatfs.FileStream;
import fatfs.FileSystem;
import fs.EndOfFileException;
import fs.ForbiddenOperation;
import fs.IFileStream;
import org.junit.Before;
import org.junit.Test;
import java.time.Instant;
import java.time.Duration;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.junit.Assert.*;

public class HardFileSystemTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        String device_name = "NewEmptyDevice";
        // Create the ssd
        boolean success = Device.buildDevice(device_name,  1024);
        assertTrue(success);

        Device d = new Device();
        try {
            d.mount(device_name);
        } catch (FileNotFoundException e) {
            fail("Disc could not be mounted");
        }
        fileSystem = (FileSystem) FileSystemFactory.createFileSystem();
        fileSystem.format(d, 2);


        // use another instance of filesystem, the data should be persistent !
        // if the test do not pass, but you expect it to pass, you can try to comment out
        // the following two lines
        fileSystem = (FileSystem) FileSystemFactory.createFileSystem();
        fileSystem.mount(d);

    }

    @Test
    public void test_scenario_4_Remove_LargeFiles_1(){

        FileStream fileStream = (FileStream) fileSystem.openFile("file.h", 'w');

        try {
            fileStream.write("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes());
        }
        catch (Exception e){
            fail("érreur écriture");
        }

        fileStream = (FileStream) fileSystem.openFile("file.h", 'r');
        try{
            byte[] output = new byte[2000];
            fileStream.read(output);
            String outputString = new String(output);
            assertTrue(outputString.startsWith("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"));
        }
        catch (Exception e){
            fail("erreur lecture");
        }
    }


    @Test //(timeout = 1000)
    public void removeLargeFileTest() throws ForbiddenOperation {
        FileStream fileStream;
        for (int k = 0; k<14; k++) {
            fileStream = (FileStream) fileSystem.openFile("file" + k + ".tst", 'a');
            for (int i = 0; i < 1170; i++) {
                fileStream.write("a".getBytes());

            }
        }

        assertEquals(16, fileSystem.listSubFile("/").size());

        for (int k = 0; k<14; k++) {
            fileSystem.removeFile("file" + k + ".tst");
        }
        assertEquals(2, fileSystem.listSubFile("/").size());
    }

    @Test
    public void lecturePlusieurPartie() throws ForbiddenOperation, EndOfFileException {
        FileStream fileStream = (FileStream) fileSystem.openFile("lecture.txt", 'w');
        byte[] toWrite = "coucou ca vas".getBytes();
        fileStream.write(toWrite);
        StringBuilder toRead = new StringBuilder();
        fileStream = (FileStream) fileSystem.openFile("lecture.txt", 'r');
        for (int i = 0; i < toWrite.length; i+=2){
            byte[] read = new byte[2];
            fileStream.read(read);
            toRead.append(new String(read));
        }
        String toWriteString = new String(toWrite);
        assertTrue(toRead.toString().startsWith(toWriteString));
    }



}
