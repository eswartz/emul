/**
 * 
 */
package v9t9.common.cassette;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author ejs
 *
 */
public class CassetteFileUtils {

	public static AudioFileFormat scanAudioFile(File audioFile) throws IOException, UnsupportedAudioFileException {
		InputStream fis = new BufferedInputStream(new FileInputStream(audioFile));
		try {
			return AudioSystem.getAudioFileFormat(fis);
		} finally {
			fis.close();
		}
	}
}
