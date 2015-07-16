package esp3.profile.a5.xml;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rplatonov
 */
@XmlRootElement
public class Scale {

    private double min;
    private double max;
    private double diff = 0.0;
    private String ref;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Scale other = (Scale) obj;
        if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(other.min)) {
            return false;
        }
        if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
            return false;
        }
        if (Double.doubleToLongBits(this.diff) != Double.doubleToLongBits(other.diff)) {
            return false;
        }
        if (!Objects.equals(this.ref, other.ref)) {
            return false;
        }
        return true;
    }

    public double getDiff() {
        return diff;
    }

    public double getMin() {
        return min;
    }
    
    public void setMin(double min) {
        this.min = min;
        diff = max - min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
        diff = max - min;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.diff) ^ (Double.doubleToLongBits(this.diff) >>> 32));
        hash = 67 * hash + Objects.hashCode(this.ref);
        return hash;
    }

    

    @Override
    public String toString() {
        return "Scale{" + "min=" + min + ", max=" + max + '}';
    }

}
