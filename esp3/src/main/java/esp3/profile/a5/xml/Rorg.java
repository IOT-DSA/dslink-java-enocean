package esp3.profile.a5.xml;

import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author rplatonov
 */
@XmlRootElement(name = "rorg")
public class Rorg {

    private int number;
    private String title;
    private List<Func> funcs;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rorg other = (Rorg) obj;
        if (this.number != other.number) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.funcs, other.funcs)) {
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

    @XmlElement(name = "func")
    public List<Func> getFuncs() {
        return funcs;
    }

    public void setFuncs(List<Func> funcs) {
        this.funcs = funcs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.number;
        hash = 97 * hash + Objects.hashCode(this.title);
        hash = 97 * hash + Objects.hashCode(this.funcs);
        return hash;
    }

    @Override
    public String toString() {
        return "Rorg{" + "number=" + number + ", title=" + title + ", funcs=" + funcs + '}';
    }

}
