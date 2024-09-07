package eu.nerdfactor.restness.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory containing {@link Bean}s for RESTness entity mapper, merger and
 * specification builder. Those fallback beans should only be used during
 * development and replaced with real implementations before production.
 *
 * @author Daniel Klug
 */
@Slf4j
@Configuration
public class RestnessEntityFactory {

	/**
	 * Provides a {@link DataMapper}{@link Bean} if none is configured.
	 *
	 * @return A {@link RestnessEntityMapper}.
	 */
	@Bean
	@ConditionalOnMissingBean(DataMapper.class)
	public DataMapper getRestnessEntityMapper() {
		log.warn("Use of fallback DataMapper. Please implement your own DataMapper for Entity to DTO mapping.");
		return new RestnessEntityMapper();
	}

	/**
	 * Provides a {@link DataMerger} {@link Bean} if none is configured.
	 *
	 * @return A {@link RestnessEntityMerger}.
	 */
	@Bean
	@ConditionalOnMissingBean(DataMerger.class)
	public DataMerger getRestnessEntityMerger() {
		log.warn("Use of fallback DataMerger. Please implement your own DataMerger for Entity updating..");
		return new RestnessEntityMerger();
	}

	/**
	 * Provides a {@link DataSpecificationBuilder} {@link Bean} if none is
	 * configured.
	 *
	 * @return A {@link DataSpecificationBuilder}.
	 */
	@Bean
	@ConditionalOnMissingBean(DataSpecificationBuilder.class)
	public DataSpecificationBuilder getRestnessSpecificationBuilder() {
		log.warn("Use of fallback SpecificationBuilder. Please implement your own SpecificationBuilder to provide proper Specifications for Entity searching.");
		return new RestnessSpecificationBuilder();
	}
}
