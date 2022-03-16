package ciexperiment.systematic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

public class CIExptRunner_Healthcare {
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		try {
			RepeatedRunner.expt_caseStudyHealthcare();
		} catch (EolModelLoadingException | EglRuntimeException | URISyntaxException | IOException
				| InterruptedException e) {
			e.printStackTrace();
		}
	}
}
