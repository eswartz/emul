package ejs.base.sound;

/**
 * Created by ejs on 3/8/16.
 */
public interface ISoundGenerator {
    /**
     * Generate the given number of samples immediately
     * @param samples
     * @return
     */
    void generate(int samples);
}
