package esp3.profile.a5.xml;

import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rplatonov
 */
@XmlRootElement(name = "datafield")
public class DataField {

    private String description;
    private String info;
    private String shortcut;
    private String data;
    private String unit;
    private int bitoffs;
    private int bitsize;
    private Range range;
    private Scale scale;
    private List<Item> items;
    private boolean learn;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataField other = (DataField) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.info, other.info)) {
            return false;
        }
        if (!Objects.equals(this.shortcut, other.shortcut)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (!Objects.equals(this.unit, other.unit)) {
            return false;
        }
        if (this.bitoffs != other.bitoffs) {
            return false;
        }
        if (this.bitsize != other.bitsize) {
            return false;
        }
        if (!Objects.equals(this.range, other.range)) {
            return false;
        }
        if (!Objects.equals(this.scale, other.scale)) {
            return false;
        }
        if (!Objects.equals(this.items, other.items)) {
            return false;
        }
        return true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        if (data != null && !data.isEmpty()) {
            this.data = data;
        }
    }

    @XmlElementWrapper(name = "enum")
    @XmlElement(name = "item")
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
        if ("LRNB".equals(shortcut)) { //speedup optimisation
            learn = true;
        }
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getBitoffs() {
        return bitoffs;
    }

    public void setBitoffs(int bitoffs) {
        this.bitoffs = bitoffs;
    }

    public int getBitsize() {
        return bitsize;
    }

    public void setBitsize(int bitsize) {
        this.bitsize = bitsize;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.info);
        hash = 97 * hash + Objects.hashCode(this.shortcut);
        hash = 97 * hash + Objects.hashCode(this.data);
        hash = 97 * hash + Objects.hashCode(this.unit);
        hash = 97 * hash + this.bitoffs;
        hash = 97 * hash + this.bitsize;
        hash = 97 * hash + Objects.hashCode(this.range);
        hash = 97 * hash + Objects.hashCode(this.scale);
        hash = 97 * hash + Objects.hashCode(this.items);
        return hash;
    }

    public boolean isLearn() {
        return learn;
    }

    @Override
    public String toString() {
        return "DataField{" + "description=" + description + ", info=" + info + ", shortcut=" + shortcut + ", data=" + data + ", unit=" + unit + ", bitoffs=" + bitoffs + ", bitsize=" + bitsize + ", range=" + range + ", scale=" + scale + ", items=" + items + '}';
    }

}
