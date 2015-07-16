package esp3.profile.a5.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rplatonov
 */
@XmlRootElement
public class Range {

    private int min;
    private int max;
    private int diff;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Range other = (Range) obj;
        if (this.min != other.min) {
            return false;
        }
        if (this.max != other.max) {
            return false;
        }
        return true;
    }

    public int getDiff() {
        return diff;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        diff = max - min;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
        diff = max - min;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.min;
        hash = 53 * hash + this.max;
        return hash;
    }

    @Override
    public String toString() {
        return "Range{" + "min=" + min + ", max=" + max + '}';
    }

}
