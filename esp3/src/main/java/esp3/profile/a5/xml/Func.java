package esp3.profile.a5.xml;

import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author rplatonov
 */
@XmlRootElement
public class Func {

    private int number;
    private String title;
    private List<EnocianType> types;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Func other = (Func) obj;
        if (this.number != other.number) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.types, other.types)) {
            return false;
        }
        return true;
    }

    @XmlTransient
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @XmlElement(name = "number")
    public String getStringNumber() {
        return Integer.toHexString(number).toUpperCase();
    }

    public void setStringNumber(String number) {
        this.number = Integer.decode(number);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name = "type")
    public List<EnocianType> getTypes() {
        return types;
    }

    public void setTypes(List<EnocianType> types) {
        this.types = types;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.number;
        hash = 29 * hash + Objects.hashCode(this.title);
        hash = 29 * hash + Objects.hashCode(this.types);
        return hash;
    }

    @Override
    public String toString() {
        return "Func{" + "number=" + number + ", title=" + title + ", types=" + types + '}';
    }
}
