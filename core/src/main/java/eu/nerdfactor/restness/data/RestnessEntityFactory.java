package eu.nerdfactor.restness.data;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory containing beans for generated entity mapper and merger
 * and specification builder.
 *
 * @author Daniel Klug
 */
@Configuration
public class RestnessEntityFactory {

	@Bean
	@ConditionalOnMissingBean(DataMapper.class)
	public DataMapper getGeneratedEntityMapper() {
		return new RestnessEntityMapper();
	}

	@Bean
	@ConditionalOnMissingBean(DataMerger.class)
	public DataMerger getGeneratedEntityMerger() {
		return new RestnessEntityMerger();
	}

	@Bean
	@ConditionalOnMissingBean(DataSpecificationBuilder.class)
	public DataSpecificationBuilder getGeneratedSpecificationBuilder() {
		return new RestnessSpecificationBuilder();
	}
}
