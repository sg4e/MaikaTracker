package sg4e.maikatracker.autotracking;

import java.io.IOException;

public interface SniMemoryReader {
    byte[] read(int snesAddress, int length) throws IOException;
}
