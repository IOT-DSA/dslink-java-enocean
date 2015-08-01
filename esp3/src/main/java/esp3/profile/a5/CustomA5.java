package esp3.profile.a5;

import esp3.profile.a5.xml.EnocianType;

public enum CustomA5 {
	//Add custom profile classes here, comma separated
	A5_20_3 (A5_20_03.class) //,
	
	
	
	
	

	
	
	
	
	
	
	;
	

	private Class<?> clazz;
	CustomA5(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	GenericProfile getProfile(EnocianType enocianType) {
		try {
			return (GenericProfile) clazz.getConstructor(EnocianType.class).newInstance(enocianType);
		} catch (Exception e) {
			return null;
		}
	}

}
