package eu.nerdfactor.restness.data;

/**
 * A simplistic entity mapper that will try to map between PersistentEntity
 * and DataTransferObjects.
 *
 * @author Daniel Klug
 */
public class RestnessEntityMapper implements DataMapper {

	@Override
	public <T> T map(Object obj, Class<T> cls) {
		if (obj instanceof PersistentEntity) {
			return (T) ((PersistentEntity<?>) obj).convertToDto();
		}
		if (obj instanceof DataTransferObject) {
			return (T) ((DataTransferObject<?>) obj).convertToEntity();
		}
		if(cls != obj.getClass()){
			throw new RuntimeException("Could not map from " + obj.getClass().getSimpleName() + " to " + cls.getSimpleName() + ". Please provide DataMapper for those classes.");
		}
		return (T) obj;
	}


}
