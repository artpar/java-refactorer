package server;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class HelloWorldConfiguration extends Configuration
{
	@NotEmpty
	private String template;

	@NotEmpty
	private String configFile;

    @JsonProperty
    public String getConfigFile() {
        return configFile;
    }

    @JsonProperty
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @NotEmpty
	private String defaultName = "Stranger";

	@JsonProperty
	public String getTemplate()
	{
		return template;
	}

	@JsonProperty
	public void setTemplate(String template)
	{
		this.template = template;
	}

	@JsonProperty
	public String getDefaultName()
	{
		return defaultName;
	}

	@JsonProperty
	public void setDefaultName(String name)
	{
		this.defaultName = name;
	}
}
