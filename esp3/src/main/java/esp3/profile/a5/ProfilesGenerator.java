package esp3.profile.a5;

import esp3.profile.Profile;
import esp3.profile.a5.xml.EnocianType;
import esp3.profile.a5.xml.Func;
import esp3.profile.a5.xml.Rorg;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rplatonov
 */
public class ProfilesGenerator {

    private final Logger logger = LoggerFactory.getLogger(ProfilesGenerator.class);

    public List<Profile> getProfiles() {
        Rorg rorg = null;
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(Rorg.class);
            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            rorg = (Rorg) unmarshaller.unmarshal(this.getClass().getResourceAsStream("/eep2.6.A5.xml")); //NOI18N
        } catch (javax.xml.bind.JAXBException ex) {
            logger.error("", ex);
        }

        List<Profile> list = new LinkedList<>();

        if (rorg != null) {
            for (Func func : rorg.getFuncs()) {
                for (EnocianType enocianType : func.getTypes()) {
                    String id = String.format("%1$s_%2$s_%3$s", rorg.getStringNumber(), func.getStringNumber(), enocianType.getStringNumber());
                    GenericProfile gprof;
                    try {
                    	CustomA5 ca5 = CustomA5.valueOf(id);
                    	gprof = ca5.getProfile(enocianType);
                    } catch (Exception e) {
                    	gprof = null;
                    }
                    if (gprof == null) gprof = new GenericProfile(id, func.getNumber(), enocianType.getNumber(), enocianType);
                    
                    list.add(gprof);
                }
            }
        }

        return list;

    }
}
