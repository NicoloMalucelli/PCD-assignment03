package ex2;

import java.io.IOException;

public interface PixelGridEventListener {
	void selectedCell(int x, int y) throws IOException;
}
