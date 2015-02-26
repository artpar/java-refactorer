package server;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration>
{
	public static void main(String[] args) throws Exception
	{
		new HelloWorldApplication().run(args);
	}

	@Override
	public String getName()
	{
		return "hello-world";
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap)
	{
		// nothing to do yet
		bootstrap.addBundle(new AssetsBundle("/res", "/app", "index.html", "static"));


	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) throws Exception {
		final HelloWorldResource resource = new HelloWorldResource(configuration.getConfigFile());

		environment.jersey().register(resource);
	}

}
