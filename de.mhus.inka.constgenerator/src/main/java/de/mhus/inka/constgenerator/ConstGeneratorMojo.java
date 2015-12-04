package de.mhus.inka.constgenerator;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(	name="ConstGenerator"
//		,defaultPhase = LifecyclePhase.COMPILE
//		,requiresDependencyResolution = ResolutionScope.COMPILE
	)

public class ConstGeneratorMojo extends AbstractMojo {
    
	@Parameter
    private boolean debug;
	
	@Parameter( defaultValue = "${project.compileSourceRoots}", readonly = true, required = true )
	private List<String> compileSourceRoots;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		Crawler crawler = new Crawler(null);
		crawler.setDebug(debug);
		
		
		for ( String root : compileSourceRoots ) {
			getLog().info( "CG " + root );
			
			File f = new File(root);
			
			crawler.crawl(f, f);
			
		}
		
		
	}

}
