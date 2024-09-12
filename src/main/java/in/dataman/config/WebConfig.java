package in.dataman.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
		String[] urlList = new String[4];
		urlList[0] = "https://datasetcollection.vercel.app";
		urlList[1] = "https://datasets.dataman.in";
		urlList[2] = "http://localhost:5173";
		urlList[3] = "https://mydatacollectionfrontend.vercel.app/";
        registry.addMapping("/**")
                .allowedOrigins(urlList)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}

