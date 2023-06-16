package ex2.message;

import utils.Pixel;

import java.util.List;
import java.util.Map;

public class GridMessage extends Message{
    private List<Pixel> pixels;
    public GridMessage(){}
    public GridMessage(String id, List<Pixel> pixels){
        super(id);
        this.pixels = pixels;
    }

    public List<Pixel> getPixels() {
        return pixels;
    }

    public void setPixels(List<Pixel> pixels) {
        this.pixels = pixels;
    }
}
