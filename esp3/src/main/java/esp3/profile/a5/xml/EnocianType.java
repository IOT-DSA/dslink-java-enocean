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
@XmlRootElement(name = "type")
public class EnocianType {

    private int number;
    private String title;
    private String status;
    private List<DataField> datafields;
    private List<Case> cases;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnocianType other = (EnocianType) obj;
        if (!Objects.equals(this.number, other.number)) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.datafields, other.datafields)) {
            return false;
        }
        if (!Objects.equals(this.cases, other.cases)) {
            return false;
        }
        return true;
    }

   // @XmlElementWrapper(name = "case")
    @XmlElement(name = "datafield")
    public List<DataField> getDatafields() {
        return datafields;
    }

    public void setDatafields(List<DataField> datafields) {
        this.datafields = datafields;
    }
    
    @XmlElement(name = "case")
    public List<Case> getCases() {
    	return cases;
    }
    
    public void setCases(List<Case> cases) {
    	this.cases = cases;
    }

    @XmlTransient
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
    @XmlElement(name="number")
    public String getStringNumber() {
        return Integer.toHexString(number).toUpperCase();
    }

    public void setStringNumber(String number) {
        this.number = Integer.decode(number);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.number);
        hash = 41 * hash + Objects.hashCode(this.title);
        hash = 41 * hash + Objects.hashCode(this.status);
        hash = 41 * hash + Objects.hashCode(this.datafields);
        hash = 41 * hash + Objects.hashCode(this.cases);
        return hash;
    }

    @Override
    public String toString() {
        return "EnocianType{" + "number=" + number + ", title=" + title + ", status=" + status + ", cases=" + cases + ", datafields=" + datafields + '}';
    }

}
