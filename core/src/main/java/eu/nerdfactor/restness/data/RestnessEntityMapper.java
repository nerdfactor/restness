package eu.nerdfactor.restness.data;

/**
 * A simplistic entity mapper that will try to map between
 * {@link PersistentEntity}s and {@link DataTransferObject}s.
 *
 * @author Daniel Klug
 */
public class RestnessEntityMapper implements DataMapper {

	/**
	 * Convert between entity and data transfer object. Will try to use the
	 * implementations of {@link PersistentEntity} and
	 * {@link DataTransferObject} for mapping between both. Otherwise, will just
	 * return the same object.
	 *
	 * @param obj The original object.
	 * @param cls The class of the converted object.
	 * @return The converted object.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T map(Object obj, Class<T> cls) {
		if (obj instanceof PersistentEntity) {
			return (T) ((PersistentEntity<?>) obj).convertToDto();
		}
		if (obj instanceof DataTransferObject) {
			return (T) ((DataTransferObject<?>) obj).convertToEntity();
		}
		if (cls != obj.getClass()) {
			throw new RuntimeException("Could not map from " + obj.getClass().getSimpleName() + " to " + cls.getSimpleName() + ". Please provide DataMapper for those classes.");
		}
		return (T) obj;
	}


}
