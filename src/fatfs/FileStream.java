package fatfs;
import fs.*;

/** Allows manipulation of IFileSystem files.
 *
 *  A file can be open in one of the following mode :
 *  'r' for read-only mode
 *  'w' for write mode (erase its current content).
 *  'a' for append mode (start writing at the end of the file)
 *
 *  Write operations can only be done in 'w' and 'a' mode.
 *  Read operations can only be done in 'r' mode.
 */
public class FileStream implements IFileStream {

    Character mode;
    DataFile file;
    DataAccess data_access;
    private int size_read;

    public FileStream(DataFile file, Character mode, DataAccess data_access){
        this.mode = mode;
        this.file = file;
        this.data_access = data_access;
        this.size_read = 0;
    }


    @Override
    public void close() {
        this.mode = null;
        this.file = null;
        this.data_access = null;
    }

    @Override
    public int read(byte[] output) throws EndOfFileException, ForbiddenOperation {
        //Cannot read directories.
        if (mode != 'r' || file.getAttribut()[4]) {
            throw new ForbiddenOperation();
        } else {
            //Copying data.
            if (size_read > file.getSize()){
                throw new EndOfFileException();
            }
            byte[] full_byte = data_access.readFileByte(this.file);
            int read = Math.min(output.length, full_byte.length-size_read);
            System.arraycopy(full_byte, size_read, output, 0, read);
            size_read += output.length;
            return read;
        }
    }

    @Override
    public int write(byte[] input) throws ForbiddenOperation {
        if (mode == 'w'){
            int writeByte = data_access.writeFileByte(file, input);
            file.setSize(writeByte);
            return writeByte;
        } else if (mode == 'a'){
            int writeByte = data_access.writeAppendFileByte(file, input);
            file.setSize(writeByte+file.getSize());
            return writeByte;
        } else {
            throw new ForbiddenOperation();
        }
    }


}
